package edu.indiana.d2i.sloan.hyper;

import edu.indiana.d2i.sloan.Configuration;
import edu.indiana.d2i.sloan.bean.VmInfoBean;
import edu.indiana.d2i.sloan.vm.VMPorts;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.openstack.OSFactory;

public class OpenStackHypervisor implements IHypervisor {

    private static String endpoint;
    private static String user;
    private static String password;
    private static String domain;
    private static String project;
    private static OSClient.OSClientV3 osClient;
    private static Flavor demoFlavor;

    static {
        endpoint = Configuration.getInstance().getString(
                Configuration.PropertyName.OPENSTACK_ENDPOINT);
        user = Configuration.getInstance().getString(
                Configuration.PropertyName.OPENSTACK_USER);
        password = Configuration.getInstance().getString(
                Configuration.PropertyName.OPENSTACK_PASSWORD);
        domain = Configuration.getInstance().getString(
                Configuration.PropertyName.OPENSTACK_DOMAIN);
        project = Configuration.getInstance().getString(
                Configuration.PropertyName.OPENSTACK_PROJECT);
        osClient = OSFactory.builderV3()
                .endpoint(endpoint)
                .credentials(user, password, Identifier.byId(domain))
                .scopeToProject(Identifier.byId(project))
                .authenticate();
        demoFlavor = osClient.compute().flavors().create(
                Builders.flavor().name("Demo").vcpus(4).disk(80).ram(2048).build()
        );

    }

    @Override
    public HypervisorResponse createVM(VmInfoBean vminfo, String pubKey, String userId) throws Exception {
        Server server = osClient.compute().servers().boot(
                Builders.server().name(vminfo.getVmid()).flavor(demoFlavor.getId()).image("imageId").build()
        );
        return null;
    }

    @Override
    public HypervisorResponse launchVM(VmInfoBean vminfo) throws Exception {
        return null;
    }

    @Override
    public HypervisorResponse queryVM(VmInfoBean vminfo) throws Exception {
        return null;
    }

    @Override
    public HypervisorResponse switchVM(VmInfoBean vminfo) throws Exception {
        return null;
    }

    @Override
    public HypervisorResponse stopVM(VmInfoBean vminfo) throws Exception {
        return null;
    }

    @Override
    public HypervisorResponse delete(VmInfoBean vminfo) throws Exception {
        return null;
    }

    @Override
    public HypervisorResponse updatePubKey(VmInfoBean vminfo, String pubKey, String userId) throws Exception {
        return null;
    }

    @Override
    public HypervisorResponse updateCustosCreds(VmInfoBean vminfo, String custos_client_id, String custos_client_secret) throws Exception {
        return null;
    }

    @Override
    public HypervisorResponse migrateVM(VmInfoBean vminfo, VMPorts vmports) throws Exception {
        return null;
    }

    @Override
    public HypervisorResponse deletePubKey(VmInfoBean vminfo, String pubKey, String userId) throws Exception {
        return null;
    }
}
