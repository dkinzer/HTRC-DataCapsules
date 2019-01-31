/*******************************************************************************
 * Copyright 2018 The Trustees of Indiana University
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
import edu.indiana.d2i.sloan.bean.VmInfoBean;
import edu.indiana.d2i.sloan.bean.VmUserRole;
import edu.indiana.d2i.sloan.db.DBOperations;
import edu.indiana.d2i.sloan.exception.NoItemIsFoundInDBException;
import edu.indiana.d2i.sloan.hyper.AddVmShareesCommand;
import edu.indiana.d2i.sloan.hyper.HypervisorProxy;
import edu.indiana.d2i.sloan.hyper.UpdatePublicKeyCommand;
import edu.indiana.d2i.sloan.utils.RolePermissionUtils;
import edu.indiana.d2i.sloan.vm.VMMode;
import edu.indiana.d2i.sloan.vm.VMRole;
import edu.indiana.d2i.sloan.vm.VMState;
import edu.indiana.d2i.sloan.vm.VMType;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Path("/updatevm")
public class UpdateVm {
	private static Logger logger = Logger.getLogger(UpdateVm.class);

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response queryVMs(@FormParam("vmId") String vmId,
			@FormParam("type") String type,
			@FormParam("consent") Boolean consent,
			@FormParam("full_access") Boolean full_access,
			@FormParam("title") String title,
			@FormParam("desc_nature") String desc_nature,
			@FormParam("desc_requirement") String desc_requirement,
			@FormParam("desc_links") String desc_links,
			@FormParam("desc_outside_data") String desc_outside_data,
			@FormParam("rr_data_files") String rr_data_files,
			@FormParam("rr_result_usage") String rr_result_usage,
			@FormParam("guids") String guids,
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest httpServletRequest) {		
		String userName = httpServletRequest.getHeader(Constants.USER_NAME);

		if (userName == null) {
			logger.error("Username is not present in http header.");
			return Response
					.status(400)
					.entity(new ErrorBean(400,
							"Username is not present in http header.")).build();
		}

		try {
			if (!RolePermissionUtils.isPermittedCommand(userName, vmId, RolePermissionUtils.API_CMD.UPDATE_VM)) {
				return Response.status(400).entity(new ErrorBean(400,
						"User " + userName + " cannot perform task "
								+ RolePermissionUtils.API_CMD.UPDATE_VM + " on VM " + vmId)).build();
			}

			logger.info("User " + userName + " tries to update the VM");
			VmInfoBean vmInfo = DBOperations.getInstance().getVmInfo(userName, vmId);

			if(!vmInfo.getType().equals(VMType.RESEARCH.getName())) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(new ErrorBean(400, "Only a " + VMType.RESEARCH.getName() +
								" capsule can be converted to a " + VMType.RESEARCH_FULL.getName() + " capsule!"))
						.build();
			}


			List<VmUserRole> vmUserRoles = DBOperations.getInstance().getRolesWithVmid(vmId, true);
			List<String> guid_list = vmUserRoles.stream().map(role -> role.getGuid()).collect(Collectors.toList());

			// If Full_access request is processed (granted or rejected)
			if(type.equals(VMType.RESEARCH_FULL.getName())) {
				if(vmInfo.isFull_access() == null) {
					return Response.status(Response.Status.BAD_REQUEST)
							.entity(new ErrorBean(400, "User has not requested full access for " +
									"this capsule!")).build();
				} else if(vmInfo.isFull_access() == true) {
					return Response.status(Response.Status.BAD_REQUEST)
							.entity(new ErrorBean(400, "This capsule is already a " +
									VMType.RESEARCH_FULL.getName() + " capsule!")).build();
				}

				if(full_access == true) {
					// if full_access is granted to particular users, then update full_access = true only for them
					// else set full_access = true for all users
					if( guids == null )
						guid_list = Arrays.asList(guids.split(","));
					DBOperations.getInstance().updateVmType(vmId, type, full_access, guid_list);
				} else {
					// if full_access is not granted , update it to null for all users
					DBOperations.getInstance().updateVmType(vmId, VMType.RESEARCH.getName(), null, guid_list);
				}

				logger.info("VM " + vmId + " of user '" + userName + "' was updated (type "
						+ type + ") in database successfully!");


				// sending public keys of users to the hypervisor who's full_access is accepted after owner's
				VmUserRole owner = vmUserRoles.stream()
						.filter(role -> role.getRole().equals(VMRole.OWNER_CONTROLLER) || role.getRole().equals(VMRole.OWNER))
						.collect(Collectors.toList()).get(0);
				List<String> user_keys = new ArrayList<>();
				for(String guid : guid_list) {
					VmUserRole user = vmUserRoles.stream()
							.filter(role -> role.getGuid().equals(guid))
							.collect(Collectors.toList()).get(0);
					if(owner.isFull_access() == true && user.isFull_access() != true) {
						user_keys.add(DBOperations.getInstance().getUserPubKey(guid));
					}
				}
				HypervisorProxy.getInstance().addCommand(
						new AddVmShareesCommand(vmInfo, userName, userName, user_keys));

				return Response.status(200).build();
			}

			if(!type.equals(VMType.RESEARCH.getName())) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(new ErrorBean(400, "Invalid capsule conversion type : " + type))
						.build();
			}

			// Processing full_access request from AG
			if(vmInfo.isFull_access()!= null && vmInfo.isFull_access() == false) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(new ErrorBean(400, "You have already requested to convert this " +
								"capsule to a " + VMType.RESEARCH_FULL.getName() + " capsule!"))
						.build();
			}  else if(vmInfo.isFull_access()!= null && vmInfo.isFull_access() == true) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(new ErrorBean(400, "This capsule is already a " +
								VMType.RESEARCH_FULL.getName() + " capsule!"))
						.build();
			} else if(!(vmInfo.getVmstate().equals(VMState.SHUTDOWN)) &&
					!(vmInfo.getVmstate().equals(VMState.RUNNING) && vmInfo.getVmmode().equals(VMMode.MAINTENANCE))) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(new ErrorBean(400, "A Capsule can be converted to a " +
								VMType.RESEARCH_FULL.getName() + " Capsule " +
								"only when in \"" + VMState.SHUTDOWN + "\" state or in " +
								" \"" + VMState.RUNNING + "\" state and \"" + VMMode.MAINTENANCE + "\" mode. " +
								"Please make sure that the Capsule is in the " +
								"right mode/state before trying to convert to a " +
								VMType.RESEARCH_FULL.getName() + " capsule"))
						.build();
			}

			// When requesting full access full_access is set to false for all users
			DBOperations.getInstance().updateVm(vmId, type, title, consent, desc_nature, desc_requirement, desc_links,
					desc_outside_data, rr_data_files, rr_result_usage, full_access);
			logger.info("VM " + vmId + " of user '" + userName + "' was updated (type "
					+ type + ") in database successfully!");

			return Response.status(200).build();
		} catch (NoItemIsFoundInDBException e) {
			logger.error(e.getMessage(), e);
			return Response
					.status(400)
					.entity(new ErrorBean(400, "VM " + vmId
							+ " is not associated with user " + userName))
					.build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Response.status(500)
					.entity(new ErrorBean(500, e.getMessage())).build();
		}
	}
}
