package er.rest;

import java.util.List;
import java.util.Map;

import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSPropertyListSerialization;

/**
 * ERXJSONRestRequestParser is an implementation of the IERXRestRequestParser interface that supports JSON document
 * requests.
 * 
 * @author mschrag
 */
public class ERXPListRestRequestParser implements IERXRestRequestParser {
	protected ERXRestRequestNode createRequestNodeForObject(String name, Object object) {
		ERXRestRequestNode requestNode = new ERXRestRequestNode(name);

		if (object == null) {
			// just leave the value null
		}
		else if (object instanceof List) {
			List list = (List) object;
			for (Object obj : list) {
				if (ERXRestUtils.isPrimitive(obj)) {
					requestNode.addChild(new ERXRestRequestNode(null, obj));
				}
				else {
					requestNode.addChild(createRequestNodeForObject(null, obj));
				}
			}
		}
		else if (object instanceof Map) {
			Map map = (Map) object;
			for (Object key : map.keySet()) {
				String strKey = (String) key;
				Object value = map.get(key);
				if (ERXRestUtils.isPrimitive(value)) {
					requestNode.addChild(new ERXRestRequestNode(strKey, value));
				}
				else {
					requestNode.addChild(createRequestNodeForObject(strKey, value));
				}
			}
		}
		else {
			throw new IllegalArgumentException("Unknown JSON value '" + object + "'.");
		}

		return requestNode;
	}

	public ERXRestRequestNode parseRestRequest(WORequest request) throws ERXRestException {
		return parseRestRequest(request.contentString());
	}

	public ERXRestRequestNode parseRestRequest(String contentStr) throws ERXRestException {
		ERXRestRequestNode rootRequestNode = null;

		if (contentStr != null && contentStr.length() > 0) {
			// MS: Support direct updating of primitive type keys -- so if you don't want to
			// wrap your request in XML, this will allow it
			// if (!contentStr.trim().startsWith("<")) {
			// contentStr = "<FakeWrapper>" + contentStr.trim() + "</FakeWrapper>";
			// }

			Object rootObj = NSPropertyListSerialization.propertyListFromString(contentStr);
			rootRequestNode = createRequestNodeForObject(null, rootObj);
		}

		return rootRequestNode;
	}
}