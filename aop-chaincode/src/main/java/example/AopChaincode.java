/*

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package example;

import static java.lang.String.format;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * 
 * @author guo
 *
 */
public class AopChaincode extends ChaincodeBase {
	private static Logger LOGGER = LoggerFactory.getLogger(AopChaincode.class);

	@Override
	public Response init(ChaincodeStub stub) {
		LOGGER.info("come to init");
		try {
			final List<String> args = stub.getStringArgs();
			LOGGER.info("the init args is {},args get 0 is {} ",args.toArray(),args.get(0));
			switch (args.get(0)) {
			case "init":
				return init(stub, args.stream().skip(1).toArray(String[]::new));
			default:
				return newErrorResponse(format("Unknown function: %s", args.get(0)));
			}
		} catch (NumberFormatException e) {
			return newErrorResponse(e.toString());
		} catch (IllegalArgumentException e) {
			return newErrorResponse(e.getMessage());
		} catch (Throwable e) {
			return newErrorResponse(e);
		}
	}

	@Override
	public Response invoke(ChaincodeStub stub) {

		try {
			final List<String> argList = stub.getStringArgs();
			final String function = argList.get(0);
			final String[] args = argList.stream().skip(1).toArray(String[]::new);
              LOGGER.info("come to invoke method with call function [{}] and args {}",function,args);
			switch (function) {
			case "init":
				return init(stub, args);
			case "invoke":
				return invoke(stub, args);
			case "transfer":
				return transfer(stub, args);
			case "put":
				for (int i = 0; i < args.length; i += 2)
					stub.putStringState(args[i], args[i + 1]);
				return newSuccessResponse();
			case "del":
				for (String arg : args)
					stub.delState(arg);
				return newSuccessResponse();
			case "query":
				return query(stub, args);
			default:
				return newErrorResponse(newErrorJson("Unknown function: %s", function));
			}

		} catch (NumberFormatException e) {
			return newErrorResponse(e.toString());
		} catch (IllegalArgumentException e) {
			return newErrorResponse(e.getMessage());
		} catch (Throwable e) {
			return newErrorResponse(e);
		}

	}

	private Response invoke(ChaincodeStub stub, String[] args) {
		System.out.println("ENTER invoke with args: " + Arrays.toString(args));
		LOGGER.info("invok args {}",Arrays.toString(args));
		if (args.length < 2) throw new IllegalArgumentException("Incorrect number of arguments. Expecting at least 2, got " + args.length);
		final String subFunction = args[0];
		final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
		LOGGER.info("invoke 's sub function is {},with subArgs [{}]",subFunction,Arrays.toString(subArgs));
		switch (subFunction) {
		case "move":
			return transfer(stub, subArgs);
		case "query":
			return query(stub, subArgs);
		case "delete":
			for (String arg : args)
				stub.delState(arg);
			return newSuccessResponse();
		default:
			return newErrorResponse(newErrorJson("Unknown invoke sub-function: %s", subFunction));
		}
	}

	private Response transfer(ChaincodeStub stub, String[] args) {
		if (args.length != 3) throw new IllegalArgumentException("Incorrect number of arguments. Expecting: transfer(from, to, amount)");
		final String fromKey = args[0];
		final String toKey = args[1];
		final String amount = args[2];

		// get state of the from/to keys
		final String fromKeyState = stub.getStringState(fromKey);
		final String toKeyState = stub.getStringState(toKey);

		// parse states as integers
		int fromAccountBalance = Integer.parseInt(fromKeyState);
		int toAccountBalance = Integer.parseInt(toKeyState);

		// parse the transfer amount as an integer
		int transferAmount = Integer.parseInt(amount);

		// make sure the transfer is possible
		if (transferAmount > fromAccountBalance) {
			throw new IllegalArgumentException("Insufficient asset holding value for requested transfer amount.");
		}

		// perform the transfer
		LOGGER.info(String.format("Tranferring %d holdings from %s to %s", transferAmount, fromKey, toKey));
		int newFromAccountBalance = fromAccountBalance - transferAmount;
		int newToAccountBalance = toAccountBalance + transferAmount;
		LOGGER.info(String.format("New holding values will be: %s = %d, %s = %d", fromKey, newFromAccountBalance, toKey, newToAccountBalance));
		stub.putStringState(fromKey, Integer.toString(newFromAccountBalance));
		stub.putStringState(toKey, Integer.toString(newToAccountBalance));
		LOGGER.info("Transfer complete.");

		return newSuccessResponse(String.format("Successfully transferred %d assets from %s to %s.", transferAmount, fromKey, toKey));
	}

	public Response init(ChaincodeStub stub, String[] args) {
		LOGGER.info("now go to init customer method ,args is {},args length is [{}]",Arrays.toString(args),args.length);
		if (args.length != 4) throw new IllegalArgumentException("Incorrect number of arguments. Expecting: init(account1, amount1, account2, amount2)");

		final String accountKey1 = args[0];
		final String accountKey2 = args[2];
		final String account1Balance = args[1];
		final String account2Balance = args[3];

		stub.putStringState(accountKey1, new Integer(account1Balance).toString());
		stub.putStringState(accountKey2, new Integer(account2Balance).toString());

		return newSuccessResponse();
	}

	public Response query(ChaincodeStub stub, String[] args) {
		if (args.length != 1) throw new IllegalArgumentException("Incorrect number of arguments. Expecting: query(account)");

		final String accountKey = args[0];
		LOGGER.info("incoming query request with accountKey [{}]",accountKey);
		byte [] payload=stub.getState(accountKey);
        String result=stub.getStringState(accountKey);
        LOGGER.info("Result is [{}]",result);
		return newSuccessResponse(result,payload);

	}

	private String newErrorJson(final String message, final Object... args) {
		return newErrorJson(null, message, args);
	}

	private String newErrorJson(final Throwable throwable, final String message, final Object... args) {
		Map<String, String> result=new HashMap<>();
		if (message != null)
			result.put("Error", String.format(message, args));
		if (throwable != null) {
			final StringWriter buffer = new StringWriter();
			throwable.printStackTrace(new PrintWriter(buffer));
			result.put("Stacktrace", buffer.toString());
		}
		return JSON.toJSONString(result);
	}

	public static void main(String[] args) throws Exception {
		LOGGER.info("Test logs-------------------");
		new AopChaincode().start(args);
	}

}
