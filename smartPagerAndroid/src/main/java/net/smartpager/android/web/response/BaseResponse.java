package net.smartpager.android.web.response;

import org.json.JSONObject;

/**
 * Stub response class for cases if no special actions with response needs 
 * @author Roman
 */
public class BaseResponse extends AbstractResponse {

	private static final long serialVersionUID = -3181539527946111892L;

	public BaseResponse(String responceContent) {
		super(responceContent);
	}

	@Override
	public void processResponse(JSONObject jsonObject) {
	}

}
