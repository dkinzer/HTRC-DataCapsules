package edu.indiana.d2i.sloan; /*******************************************************************************
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

import edu.indiana.d2i.sloan.bean.ErrorBean;
import edu.indiana.d2i.sloan.bean.ImageInfoBean;
import edu.indiana.d2i.sloan.db.DBOperations;
import edu.indiana.d2i.sloan.image.ImageState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

@Path("/checkimagename")
public class CheckImageName {
	private static Logger logger = LoggerFactory.getLogger(CheckImageName.class);

//	@POST
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkImage(@Context HttpHeaders httpHeaders,
							   @QueryParam("imageName") String imageName,
							   @Context HttpServletRequest httpServletRequest) {
		String userName = httpServletRequest.getHeader(Constants.USER_NAME);

		// check if username exists
		if (userName == null) {
			logger.error("Username is not present in http header.");
			return Response
					.status(500)
					.entity(new ErrorBean(500,
							"Username is not present in http header.")).build();
		}

		if (imageName == null) {
			logger.error("Invalid Image Name! Image Name is null");
			return Response.status(400).entity( "Invalid Image Name!").build();
		}

		try {
			if(isImageNameAvailable(imageName)){
				return Response.status(200).build();
			}else{
				logger.error("Image Name is not available!");
				return Response.status(400).entity( "Image Name is not available!").build();
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Response.status(500)
					.entity(new ErrorBean(500, e.getMessage())).build();
		}
	}

	public Boolean isImageNameAvailable(String imageName){
		boolean imageNameAvailable = true;
		try {
			List<ImageInfoBean> imageInfo = DBOperations.getInstance().getImagesInfo();
			for(ImageInfoBean imageInfoBean: imageInfo){
				if(imageInfoBean.getImageName().equals(imageName) && (imageInfoBean.getImageStatus() != ImageState.DELETED || imageInfoBean.getImageStatus() != ImageState.DELETE_PENDING)){
					logger.debug(imageInfoBean.getImageStatus().toString());
					imageNameAvailable = false ;
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return imageNameAvailable;
	}
}
