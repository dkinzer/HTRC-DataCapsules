package edu.indiana.d2i.sloan.hyper;

import org.apache.log4j.Logger;

import edu.indiana.d2i.sloan.bean.VmInfoBean;
import edu.indiana.d2i.sloan.db.DBOperations;
import edu.indiana.d2i.sloan.exception.RetriableException;
import edu.indiana.d2i.sloan.exception.ScriptCmdErrorException;
import edu.indiana.d2i.sloan.vm.VMState;
import edu.indiana.d2i.sloan.vm.VMStateManager;

public class DeleteVMCommand extends HypervisorCommand {
	private static Logger logger = Logger.getLogger(DeleteVMCommand.class);
	private final String username;

	public DeleteVMCommand(String username, VmInfoBean vminfo) {
		super(vminfo);
		this.username = username;
	}

	@Override
	public void execute() throws Exception {
		try {
			HypervisorResponse resp = hypervisor.delete(vminfo);

			if (logger.isDebugEnabled()) {
				logger.debug(resp.toString());
			}

			if (resp.getResponseCode() != 0) {
				throw new ScriptCmdErrorException(String.format(
						"Failed to excute command:\n%s ", resp));
			}

		} catch (Exception e) {
			throw new RetriableException(e.getMessage(), e);
		}

		// no need to update VM' state and mode since it is going to be deleted
		/* Also restore user quota after deleting the VM */
		DBOperations.getInstance().deleteVMs(username, vminfo);
	}

	@Override
	public void cleanupOnFailed() throws Exception {
		VMStateManager.getInstance().transitTo(vminfo.getVmid(),
				VMState.DELETING, VMState.ERROR);

	}

	@Override
	public String toString() {
		return "deletevm " + vminfo;
	}
}
