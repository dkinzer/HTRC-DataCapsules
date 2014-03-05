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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;

import edu.indiana.d2i.sloan.bean.ErrorBean;
import edu.indiana.d2i.sloan.bean.ResultBean;
import edu.indiana.d2i.sloan.db.DBOperations;
import edu.indiana.d2i.sloan.exception.NoItemIsFoundInDBException;
import edu.indiana.d2i.sloan.exception.ResultExpireException;

@Path("/download")
public class DownloadResult {
	private static Logger logger = Logger.getLogger(DownloadResult.class);
	
	private class ResultOutputStream implements StreamingOutput {
		private final InputStream input;
		
		public ResultOutputStream(InputStream input) {
			this.input = input;
		}
		
		@Override
		public void write(OutputStream output) throws IOException,
				WebApplicationException {
			byte[] buf = new byte[1024];
			int length = 0;
			while ((length = input.read(buf)) != -1) {
				output.write(buf, 0, length);
			}
		}
	}
	
	@GET
	public Response getResourcePost(
		@QueryParam("randomid") String randomid,
		@Context HttpHeaders httpHeaders,
		@Context HttpServletRequest httpServletRequest) {
		if (randomid == null) {
			return Response.status(400)
				.entity(new ErrorBean(400, "Invalid download URL!")).build();
		}		
		
		try {
			ResultBean result = DBOperations.getInstance().getResult(randomid);
			logger.info("Result with " + randomid + " is being downloaded.");
			
			// check if result expires
			long currentT = new java.util.Date().getTime();
			long startT = result.getStartdate().getTime();
			long span = Configuration.getInstance().getLong(
				Configuration.PropertyName.RESULT_EXPIRE_IN_SECOND);
			if (span > 0 && ((currentT-startT)/1000) > span) 
				throw new ResultExpireException(randomid + " expires!");			
			
			return Response.ok(new ResultOutputStream(result.getInputstream())).
				header("Content-Type", "application/zip").
				header("Content-Disposition", "filename=\"result.zip\"").build();
		} catch (NoItemIsFoundInDBException e) {
			logger.error("Invalid " + randomid + " attempts", e);
			return Response.status(404).entity("Invalid download URL!").build();
		} catch (ResultExpireException e) {
			logger.error(e.getMessage(), e);
			return Response.status(404).entity("Result expires!").build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Response.status(500).entity("Internal error.").build();
		}
	}
}
