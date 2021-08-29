package Server;

import DCMSApp.*;
import FrontEnd.FrontEndImpl;
import org.omg.CosNaming.*;
import java.io.File;
import java.io.IOException;
import Conf.Const;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import Conf.Location;

public class RunServer {
	static DCMS ref;

	static {
		try {
			Runtime.getRuntime().exec("orbd -ORBInitialPort 1050 -ORBInitialHost localhost");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void init() {
		new File(Const.LOG_DIR + "Server_FrontEnds").mkdir();
		new File(Const.LOG_DIR + "PRIMARY_SERVER").mkdir();
		new File(Const.LOG_DIR + "PRIMARY_SERVER"+ "\\"+ Location.MTL.toString()).mkdir();
		new File(Const.LOG_DIR + "PRIMARY_SERVER"+ "\\"+ Location.LVL.toString()).mkdir();
		new File(Const.LOG_DIR + "PRIMARY_SERVER"+ "\\"+ Location.DDO.toString()).mkdir();
		new File(Const.LOG_DIR + "REPLICA1_SERVER").mkdir();
		new File(Const.LOG_DIR + "REPLICA1_SERVER"+ "\\"+ Location.MTL.toString()).mkdir();
		new File(Const.LOG_DIR + "REPLICA1_SERVER"+ "\\"+ Location.LVL.toString()).mkdir();
		new File(Const.LOG_DIR + "REPLICA1_SERVER"+ "\\"+ Location.DDO.toString()).mkdir();
		new File(Const.LOG_DIR + "REPLICA2_SERVER").mkdir();
		new File(Const.LOG_DIR + "REPLICA2_SERVER"+ "\\"+ Location.MTL.toString()).mkdir();
		new File(Const.LOG_DIR + "REPLICA2_SERVER"+ "\\"+ Location.LVL.toString()).mkdir();
		new File(Const.LOG_DIR + "REPLICA2_SERVER"+ "\\"+ Location.DDO.toString()).mkdir();
		new File(Const.LOG_DIR + "ReplicasResponse").mkdir();
	}

	public static void main(String args[]) {
		try {
			init();
			ORB orb = ORB.init(args, null);
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();
			FrontEndImpl serverFE = new FrontEndImpl();
			org.omg.CORBA.Object feRef = rootpoa.servant_to_reference(serverFE);
			ref = DCMSHelper.narrow(feRef);
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			NameComponent fePath[] = ncRef.to_name("FE");
			ncRef.rebind(fePath, ref);
			System.out.println("Distributed Class Management System Servers are up and running");
			orb.run();
		}
		catch (Exception e) {
			System.err.println("Exception" + e);
			e.printStackTrace(System.out);
		}
	}
}