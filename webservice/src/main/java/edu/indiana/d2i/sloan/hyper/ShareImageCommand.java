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
package edu.indiana.d2i.sloan.hyper;

import edu.indiana.d2i.sloan.bean.ImageInfoBean;
import edu.indiana.d2i.sloan.bean.VmInfoBean;
import edu.indiana.d2i.sloan.db.DBOperations;
import edu.indiana.d2i.sloan.exception.ScriptCmdErrorException;
import edu.indiana.d2i.sloan.utils.RetriableTask;
import edu.indiana.d2i.sloan.vm.PortsPool;
import edu.indiana.d2i.sloan.vm.VMPorts;
import edu.indiana.d2i.sloan.vm.VMState;
import edu.indiana.d2i.sloan.vm.VMStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Callable;

public class ShareImageCommand extends HypervisorCommand {
	private static final Logger logger = LoggerFactory.getLogger(ShareImageCommand.class);
	private final String operator;
	private final ImageInfoBean imageInfoBean;


	public ShareImageCommand(VmInfoBean vminfo, ImageInfoBean imageInfoBean, String operator) throws Exception {
		super(vminfo);
		this.operator = operator;
		this.imageInfoBean = imageInfoBean;
	}

	@Override
	public void execute() throws Exception {
		HypervisorResponse resp = hypervisor.shareImage(vminfo, imageInfoBean);
		logger.info(resp.toString());

		if (resp.getResponseCode() != 0) {
			throw new ScriptCmdErrorException(String.format(
					"Failed to excute command:\n%s ", resp));
		}

		/*
			If image share is successful, add image details to image table.
		 */
		RetriableTask<Void> r = new RetriableTask<Void>(
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {

					DBOperations.getInstance().addImage(imageInfoBean);

					VMStateManager.getInstance().transitTo(vminfo.getVmid(),
							vminfo.getVmstate(), VMState.SHUTDOWN, operator);

						if (logger.isDebugEnabled()) {
							logger.debug(String.format(
									"Image of VM (vmid = %s) is shared. New image name - %s, image status - %s ",
									vminfo.getVmid(), imageInfoBean.getImageName(), imageInfoBean.getImageStatus()));
						}

						return null;
				}
			},  1000, 3, 
			new HashSet<String>(Arrays.asList(java.sql.SQLException.class.getName())));
		r.call();
	}

	@Override
	public void cleanupOnFailed() throws Exception {
		/*
			If image share fails, update VM's state to Error
		 */
		RetriableTask<Void> r = new RetriableTask<Void>(
			new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					logger.error("Error occurred while sharing the image of VM " + vminfo.getVmid());

					VMStateManager.getInstance().transitTo(vminfo.getVmid(),
							vminfo.getVmstate(), VMState.ERROR, operator);

					return null;
				}
			},  1000, 3, 
			new HashSet<String>(Arrays.asList(java.sql.SQLException.class.getName())));
		r.call();
	}

	@Override
	public String toString() {
		return "Share Image of VM " + vminfo;
	}
}
