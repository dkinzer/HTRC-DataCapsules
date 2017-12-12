/*******************************************************************************
 * Copyright 2017 The Trustees of Indiana University
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
import edu.indiana.d2i.sloan.bean.ResultBean;
import edu.indiana.d2i.sloan.db.DBOperations;
import edu.indiana.d2i.sloan.exception.NoItemIsFoundInDBException;
import edu.indiana.d2i.sloan.exception.ResultExpireException;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.sql.SQLException;

@Path("/retrieveresultfile")
public class RetrieveResultFile {
	private static Logger logger = Logger.getLogger(RetrieveResultFile.class);

	private class ResultOutputStream implements StreamingOutput {
		private final InputStream input;

		public ResultOutputStream(InputStream input) {
			this.input = input;
		}

		@Override
		public void write(OutputStream output) throws IOException, WebApplicationException
		{
			byte[] buf = new byte[1024];
			int length = 0;
			while ((length = input.read(buf)) != -1) {
				output.write(buf, 0, length);
			}
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadResult(
			@QueryParam("randomid") String randomid,
			@QueryParam("filename") String filename,
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest httpServletRequest)
	{
		if (randomid == null) {
			return Response.status(400)
					.entity(new ErrorBean(400, "Invalid download URL!")).build();
		}

		if (filename==null || filename.length()==0){
			filename = String.format("result-", randomid, ".txt");
		}

		try {
			ResultBean result = DBOperations.getInstance().getResult(randomid);

			writeFile(fetchData(randomid),filename);
			return Response.status(200).entity(new ResultOutputStream(result.getInputstream())).build();

		} catch (NoItemIsFoundInDBException e) {
			logger.error("Invalid " + randomid + " attempts", e);
			return Response.status(404).entity("Invalid download URL!").build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Response.status(500).entity("Internal error.").build();
		}
	}

	//fetch content from db to write into output file
	public String fetchData(String resultid)
			throws java.text.ParseException, SQLException, NoItemIsFoundInDBException, IOException
	{

		ResultBean result =  DBOperations.getInstance().getResult(resultid);

		InputStream dataField = result.getInputstream();
		char[] buffer = new char[1024];

		StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(dataField, "UTF-8");
		int l= in.read(buffer,0,buffer.length);
		while(l >= 0){
			out.append(buffer,0,l);
			l=in.read(buffer, 0, l);
		}

		//Output target path?
		return out.toString();
	}

	public void writeFile(String content, String filename) {

		try {
			PrintWriter out = new PrintWriter(filename);
			out.println(content);

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(),e);
		}
	}

}