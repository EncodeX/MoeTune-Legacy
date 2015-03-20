package moetune.core;

/**
 * Created by Encode_X on 14-9-18.
 */
public class MoeTuneMusicCoverUrl {
	private String small;
	private String medium;
	private String square;
	private String large;

	public MoeTuneMusicCoverUrl(String small, String medium, String square, String large) {
		this.small = small;
		this.medium = medium;
		this.square = square;
		this.large = large;
	}

	public String getSmall() {
		return small;
	}

	public void setSmall(String small) {
		this.small = small;
	}

	public String getMedium() {
		return medium;
	}

	public void setMedium(String medium) {
		this.medium = medium;
	}

	public String getSquare() {
		return square;
	}

	public void setSquare(String square) {
		this.square = square;
	}

	public String getLarge() {
		return large;
	}

	public void setLarge(String large) {
		this.large = large;
	}
}
