package moetune.core;

/**
 * Created by Encode_X on 14-10-9.
 */
public interface MoeTuneConstants {
	public class Config{
		public final static String PREFERENCE_NAME = "moe_tune_preference";
		public final static String IS_MEMBER_REGISTERED = "is_member_registered";
		public final static String IS_WIFI_CONNECTED = "is_wifi_connected";
		public final static int NOTIFICATION_ID = 1517;

		public final static int USER_TYPE_GUEST = 0;
		public final static int USER_TYPE_MEMBER = 1;
	}

	public class Actions{
		public final static int LOGIN_REQUEST = 1000;
		public final static int LOGIN_SUCCESS = 1001;
		public final static int LOGIN_FAILED = 1002;
	}

	public class NetworkState{
		public final static int NETWORK_WIFI = 1000;
		public final static int NETWORK_MOBILE = 1001;
		public final static int NETWORK_UNKNOWN = 1002;
	}

	public class Error {
		public final static int PLAYER_CANNOT_CONNECT_NETWORK = 1000;
		public final static int LIST_GET_ERROR = 1010;
		public final static int LIST_SIGN_ERROR = 1011;
	}
}
