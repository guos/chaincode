package example;

import static org.junit.Assert.fail;

import java.util.Arrays;

import org.hyperledger.fabric.shim.Chaincode.Response;
import org.hyperledger.fabric.shim.Chaincode.Response.Status;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class AopChaincodeTest {

	@Test
	public void testInitChaincodeStub() {
		ChaincodeStub stub = Mockito.mock(ChaincodeStub.class);
		String[] value = { "init", "a", "100", "b", "200" };
		Mockito.when(stub.getStringArgs()).thenReturn(Arrays.asList(value));
		AopChaincode chaincode = new AopChaincode();
		Response response = chaincode.init(stub);
		Assert.assertTrue(response.getStatus().equals(Status.SUCCESS));

	}

	@Test
	public void testInvokeChaincodeStub() {
		ChaincodeStub stub = Mockito.mock(ChaincodeStub.class);
		String[] value = { "init", "a", "100", "b", "200" };
		Mockito.when(stub.getStringArgs()).thenReturn(Arrays.asList(value));
		AopChaincode chaincode = new AopChaincode();
		Response response = chaincode.invoke(stub);
		Assert.assertTrue(response.getStatus().equals(Status.SUCCESS));
	}

	@Test
	public void testInvokeChaincodeStubInvoke() {
		ChaincodeStub stub = Mockito.mock(ChaincodeStub.class);
		String[] value = { "invoke", "move", "a", "b", "100" };
		Mockito.when(stub.getStringArgs()).thenReturn(Arrays.asList(value));
		Mockito.when(stub.getStringState("a")).thenReturn("100");
		Mockito.when(stub.getStringState("b")).thenReturn("100");
		AopChaincode chaincode = new AopChaincode();
		Response response = chaincode.invoke(stub);
		System.out.println(response.getMessage());
		Assert.assertEquals("Successfully transferred 100 assets from a to b.", response.getMessage());
	}

	@Test
	public void testInitChaincodeStubStringArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testQuery() {

		AopChaincode chaincode = new AopChaincode();
		ChaincodeStub stub = Mockito.mock(ChaincodeStub.class);
		String[] args = { "a" };
		Mockito.when(stub.getStringState("a")).thenReturn("100");
		Mockito.when(stub.getState("a")).thenReturn("100".getBytes());
		Response response = chaincode.query(stub, args);
		Assert.assertEquals("100", response.getStringPayload());

	}

	@Test
	public void testMain() {
		fail("Not yet implemented");
	}

}
