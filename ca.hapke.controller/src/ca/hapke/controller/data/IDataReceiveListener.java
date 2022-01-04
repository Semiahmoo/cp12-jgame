package ca.hapke.controller.data;

/**
 * @author Mr. Hapke
 */
public interface IDataReceiveListener {

	public void serverOnline();

	public void serverOffline();

	public void accelGyroUpdated();

	public void sentenceReceived(String sentence);

	public void serverAbort(String msg);

}
