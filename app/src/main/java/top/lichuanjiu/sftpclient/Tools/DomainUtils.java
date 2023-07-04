package top.lichuanjiu.sftpclient.Tools;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class DomainUtils {
    /**
     * 判断一个字符串是否是域名
     *
     * @param str 需要判断的字符串
     * @return true 是域名，false 不是域名
     */
    public boolean isDomainName(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        String domainNamePattern = "^(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$";
        return Pattern.matches(domainNamePattern, str);
    }

    /**
     * 判断一个字符串是否是IPV4地址
     *
     * @param str 需要判断的字符串
     * @return true 是一个IPV4地址，false 不是一个IPV4地址
     */
    public boolean isIPV4(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        String ipv4Pattern = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";
        return Pattern.matches(ipv4Pattern, str);
    }

    /**
     * 判断一个数字是否是端口
     *
     * @param port 需要判断的数值
     * @return true 是一个端口，false 不是一个端口
     */
    public boolean isPort(int port) {
        return port > 0 && port <= 65535;
    }

    /**
     * 判断一个字符串是否是一个端口
     *
     * @param port 需要判断的字符串
     * @return true 是一个端口，false 不是一个端口
     */
    public boolean isPort(String port) {
        if (port == null || port.isEmpty()) {
            return false;
        }

        try {
            int portNumber = Integer.parseInt(port);
            return isPort(portNumber);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 将一个字符串转换成int，转换结果大于0
     *
     * @param num 需要转换的数字
     * @return 如果转换成功，返回对应的数值，如果转换失败或转换后的值小于0，返回-1
     */
    public static int toInt(String num) {
        if (num == null || num.isEmpty()) {
            return -1;
        }

        try {
            int result = Integer.parseInt(num);
            if (result > 0) {
                return result;
            }
        } catch (NumberFormatException e) {
            // Ignore and return -1
        }
        return -1;
    }

    /**
     * 用来将一个域名解析成对应的IP_V4地址
     *
     * @param domainName 需要解析的域名
     * @return 返回一个IP_V4地址，如果解析失败返回null
     */
    public String domainNameResolution(String domainName) {
        if (domainName == null || domainName.isEmpty()) {
            return null;
        }

        try {
            InetAddress address = InetAddress.getByName(domainName);
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
