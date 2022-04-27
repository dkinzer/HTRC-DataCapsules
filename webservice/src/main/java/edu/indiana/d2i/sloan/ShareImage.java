/*******************************************************************************
 * Copyright 2014 The Trustees of Indiana University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.indiana.d2i.sloan;

import edu.indiana.d2i.sloan.bean.ErrorBean;
import edu.indiana.d2i.sloan.bean.ImageInfoBean;
import edu.indiana.d2i.sloan.bean.VmInfoBean;
import edu.indiana.d2i.sloan.db.DBOperations;
import edu.indiana.d2i.sloan.exception.NoItemIsFoundInDBException;
import edu.indiana.d2i.sloan.hyper.HypervisorProxy;
import edu.indiana.d2i.sloan.hyper.ShareImageCommand;
import edu.indiana.d2i.sloan.image.ImageState;
import edu.indiana.d2i.sloan.utils.RolePermissionUtils;
import edu.indiana.d2i.sloan.vm.VMState;
import edu.indiana.d2i.sloan.vm.VMStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.UUID;

@Path("/shareimage")
public class ShareImage {
	private static final Logger logger = LoggerFactory.getLogger(ShareImage.class);
	private static final String ADMIN = "admin";
	private final java.text.SimpleDateFormat DATE_FORMATOR =
			new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response shareImage(@FormParam("vmId") String vmId,
							   @FormParam("imageName") String imageName,
							   @FormParam("imageDescription") String imageDescription,
							   @FormParam("public") Boolean isPublic,
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest httpServletRequest) {
		String userName = httpServletRequest.getHeader(Constants.USER_NAME);

		if (userName == null) {
			logger.error("Username is not present in http header.");
			return Response
					.status(500)
					.entity(new ErrorBean(500,
							"Username is not present in http header.")).build();
		}

		if (vmId == null || imageName == null || imageDescription == null || isPublic == null) {
			logger.error("One or two form parameters are empty!");
			return Response.status(400)
					.entity(new ErrorBean(400, "One or two form parameters are empty!"))
					.build();
		}

		try {
			if (!RolePermissionUtils.isPermittedCommand(userName, vmId, RolePermissionUtils.API_CMD.SHARE_IMAGE)) {
				logger.error("User " + userName + " cannot perform task "
						+ RolePermissionUtils.API_CMD.SHARE_IMAGE + " on VM " + vmId);
				return Response.status(400).entity(new ErrorBean(400,
						"User " + userName + " cannot perform task "
								+ RolePermissionUtils.API_CMD.SHARE_IMAGE + " on VM " + vmId)).build();
			}

			VmInfoBean vmInfo = DBOperations.getInstance().getVmInfo(userName, vmId);

			CheckImageName checkImageName = new CheckImageName();
			if (!checkImageName.isImageNameAvailable(imageName)){
				logger.error("Image Name is not available!");
				return Response
						.status(400)
						.entity(new ErrorBean(400, "Image Name is not available!"))
						.build();
			}

			if (VMStateManager.isPendingState(vmInfo.getVmstate()) ||!VMStateManager.getInstance().transitTo(vmId, vmInfo.getVmstate(), VMState.IMAGE_SHARE_PENDING, userName)) {
				logger.error("Cannot share image of VM " + vmId
						+ " when it is " + vmInfo.getVmstate());
				return Response
						.status(400)
						.entity(new ErrorBean(400, "Cannot share image of VM " + vmId
								+ " when it is " + vmInfo.getVmstate()))
						.build();
			}

			if(!DBOperations.getInstance().imageQuotaNotExceedLimit(userName)){
				logger.error("User's Image share left quota is exceeded!");
				return Response
						.status(400)
						.entity(new ErrorBean(400, "User's Image share left quota is exceeded!"))
						.build();
			}

			logger.info(userName + " requests to share the image of VM " + vmInfo.getVmid());
			vmInfo.setVmState(VMState.IMAGE_SHARE_PENDING);
			String imageId = UUID.randomUUID().toString();
			String newImagePath = Configuration.PropertyName.HOST_IMAGE_DIR + imageId + ".img";
			java.util.Date dt = new java.util.Date();
			String created_at = DATE_FORMATOR.format(dt);
			ImageInfoBean imageInfoBean = new ImageInfoBean(imageId, imageName, ImageState.SHARE_PENDING, imageDescription, newImagePath, null, null, vmId, isPublic, userName,created_at,created_at);
			HypervisorProxy.getInstance().addCommand(new ShareImageCommand(vmInfo, imageInfoBean, userName));
			return Response.status(200).entity(imageInfoBean).build();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return Response.status(500).
					entity(new ErrorBean(500, "Internal error - " + e.getMessage())).build();
		} catch (NoItemIsFoundInDBException e) {
			logger.error(e.getMessage(), e);
			return Response
					.status(400)
					.entity(new ErrorBean(400, "Cannot find VM " + vmId
							+ " associated with username " + userName)).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Response.status(500)
					.entity(new ErrorBean(500, e.getMessage())).build();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateImage(@FormParam("imageId") String imageId,
							   @FormParam("imageName") String imageName,
							   @FormParam("imageDescription") String imageDescription,
							   @FormParam("public") Boolean isPublic,
							   @Context HttpHeaders httpHeaders,
							   @Context HttpServletRequest httpServletRequest) {
		String userName = httpServletRequest.getHeader(Constants.USER_NAME);

		if (userName == null) {
			logger.error("Username is not present in http header.");
			return Response
					.status(500)
					.entity(new ErrorBean(500,
							"Username is not present in http header.")).build();
		}

		if (imageId == null || imageName == null || imageDescription == null || isPublic == null) {
			logger.error("One or two form parameters are empty!");
			return Response.status(400)
					.entity(new ErrorBean(400, "One or two form parameters are empty!"))
					.build();
		}



		try {
			ImageInfoBean imageInfo = DBOperations.getInstance().getImageInfo(imageId);

			if (!imageInfo.getOwner().equals(userName)) {
				logger.error("User " + userName + " cannot update the image with the Image ID " + imageId);
				return Response.status(400).entity(new ErrorBean(400,
						"User " + userName + " cannot update the image with the Image ID " + imageId)).build();
			}


			if (!imageInfo.getImageStatus().equals(ImageState.ACTIVE)) {
				logger.error("Cannot update the image with the image ID " + imageId+ " when it's not in " + ImageState.ACTIVE.toString() + " state.");
				return Response
						.status(400)
						.entity(new ErrorBean(400, "Cannot update the image with the image ID " + imageId+ " when it's not in " + ImageState.ACTIVE.toString() + " state."))
						.build();
			}

			CheckImageName checkImageName = new CheckImageName();
			if (!imageName.equals(imageInfo.getImageName()) && !checkImageName.isImageNameAvailable(imageName)){
				logger.error("Image Name is not available!");
				return Response
						.status(400)
						.entity(new ErrorBean(400, "Image Name is not available!"))
						.build();
			}

			logger.info(userName + " requests to update the image with the image ID " + imageId);
			DBOperations.getInstance().updateImage(imageId,imageName,imageDescription,isPublic);
			imageInfo = DBOperations.getInstance().getImageInfo(imageId);
			return Response.status(200).entity(imageInfo).build();

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return Response.status(500).
					entity(new ErrorBean(500, "Internal error - " + e.getMessage())).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Response.status(500)
					.entity(new ErrorBean(500, e.getMessage())).build();
		}
	}
}
