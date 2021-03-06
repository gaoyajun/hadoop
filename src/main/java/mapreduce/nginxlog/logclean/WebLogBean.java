package mapreduce.nginxlog.logclean;

/**
 * 该JavaBean数据结构包含8个String类型的属性:
 * IP_addr time method request_URL request_protocol respond_code respond_data requst_come_from browser
 */
public class WebLogBean {

	private String IP_addr;
	private String time;
	private String method;
	private String request_URL;
	private String request_protocol;
	private String respond_code;
	private String respond_data;
	private String requst_come_from;
	private String browser;

	public String getIP_addr() {
		return IP_addr;
	}

	public void setIP_addr(String iP_addr) {
		IP_addr = iP_addr;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getRequest_URL() {
		return request_URL;
	}

	public void setRequest_URL(String request_URL) {
		this.request_URL = request_URL;
	}

	public String getRequest_protocol() {
		return request_protocol;
	}

	public void setRequest_protocol(String request_protocol) {
		this.request_protocol = request_protocol;
	}

	public String getRespond_code() {
		return respond_code;
	}

	public void setRespond_code(String respond_code) {
		this.respond_code = respond_code;
	}

	public String getRespond_data() {
		return respond_data;
	}

	public void setRespond_data(String respond_data) {
		this.respond_data = respond_data;
	}

	public String getRequst_come_from() {
		return requst_come_from;
	}

	public void setRequst_come_from(String requst_come_from) {
		this.requst_come_from = requst_come_from;
	}

	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

	@Override
	public String toString() {
		return IP_addr + " " + time + " " + method + " "
				+ request_URL + " " + request_protocol + " " + respond_code
				+ " " + respond_data + " " + requst_come_from + " " + browser;
	}


}