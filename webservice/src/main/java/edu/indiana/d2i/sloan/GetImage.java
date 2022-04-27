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
import edu.indiana.d2i.sloan.db.DBOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/getimage")
public class GetImage {
	private static Logger logger = LoggerFactory.getLogger(GetImage.class);

//	@POST
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getImage(@QueryParam("imageId") String imageId,
			@Context HttpHeaders httpHeaders,
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

		if (imageId == null) {
			logger.error("Invalid Image ID! Image ID is null");
			return Response.status(400).entity( "Invalid Image ID!").build();
		}

		try {
			ImageInfoBean imageInfo = DBOperations.getInstance().getImageInfo(imageId);
			if (imageInfo.getPublic() || imageInfo.getOwner().equals(userName)){
				return Response.status(200).entity(imageInfo).build();
			} else {
				logger.error("User " + userName + " is not allowed to get image information of image ID " + imageId);
				return Response.status(400).entity( "User " + userName + " is not allowed to get image information of image ID " + imageId).build();
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Response.status(500)
					.entity(new ErrorBean(500, e.getMessage())).build();
		}
	}
}
