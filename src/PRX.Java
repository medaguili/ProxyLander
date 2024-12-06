import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PRX {

    private static final List<String> URLS = Arrays.asList(
            "https://www.socks-proxy.net/",
            "https://www.proxy-list.download/SOCKS5",
            "https://www.proxy-list.download/SOCKS4",
            "https://github.com/zloi-user/hideip.me/blob/main/socks5.txt",
            "https://github.com/zloi-user/hideip.me/blob/main/socks4.txt",
            "https://github.com/proxifly/free-proxy-list/blob/main/proxies/protocols/socks5/data.txt",
            "https://github.com/proxifly/free-proxy-list/blob/main/proxies/protocols/socks4/data.txt",
            "https://github.com/sunny9577/proxy-scraper/blob/master/generated/socks4_proxies.txt",
            "https://github.com/sunny9577/proxy-scraper/blob/master/generated/socks5_proxies.txt",
            "https://github.com/roosterkid/openproxylist/blob/main/SOCKS4_RAW.txt",
            "https://github.com/roosterkid/openproxylist/blob/main/SOCKS5_RAW.txt",
            "https://github.com/MuRongPIG/Proxy-Master/blob/main/socks4.txt",
            "https://github.com/MuRongPIG/Proxy-Master/blob/main/socks5.txt",
            "https://github.com/Zaeem20/FREE_PROXIES_LIST/blob/master/socks4.txt",
            "https://github.com/TheSpeedX/PROXY-List/blob/master/socks5.txt",
            "https://www.proxyrack.com/free-proxy-list/",
            "https://free.proxy-sale.com/en/socks4/",
            "https://proxydb.net/?protocol=socks4&protocol=socks5",
            "https://advanced.name/freeproxy?type=socks4",
            "https://advanced.name/freeproxy?type=socks5",
            "https://spys.one/en/socks-proxy-list/",
            "https://www.socks-proxy.net/"
    );

    private static final ExecutorService executor = Executors.newFixedThreadPool(20);

    public static void main(String[] args) {
        System.out.println("Starting proxy scraping...");

        Set<String> allProxies = new HashSet<>();
        for (String url : URLS) {
            System.out.println("Scraping URL: " + url);
            allProxies.addAll(scrapeProxies(url));
        }
        System.out.println("Total Proxies Found: " + allProxies.size());
        saveProxiesToFile(allProxies, "all_proxies.txt");

        System.out.println("Testing proxies...");
        List<ProxyInfo> workingProxies = testProxiesInBatches(allProxies, 5000, 300);
        workingProxies.sort(Comparator.comparingLong(ProxyInfo::getResponseTime));

        //saveWorkingProxiesToFile(workingProxies, "working_proxies.txt");

        System.out.println("Fastest Working Proxies:");
        workingProxies.forEach(proxy ->
                System.out.println(proxy.getProxy() + " - " + proxy.getResponseTime() + "ms")
        );

        System.out.println("Process completed. Results saved to 'all_proxies.txt' and 'working_proxies.txt'.");
        executor.shutdown();
    }

    private static Set<String> scrapeProxies(String url) {
        Set<String> proxies = new HashSet<>();
        try {
            String content = fetchPageContent(url);
            proxies.addAll(findProxies(content));
            proxies.addAll(findProxiesInTables(content));
            proxies.addAll(findProxiesUsingRegex(content));
        } catch (Exception e) {
            System.out.println("Error scraping URL: " + url + " - " + e.getMessage());
        }
        return proxies;
    }

    private static String fetchPageContent(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return content.toString();
        }
    }

    private static Set<String> findProxies(String content) {
        Set<String> proxies = new HashSet<>();
        Matcher matcher = Pattern.compile("(socks4|socks5)://([\\d.]+):(\\d+)").matcher(content);
        while (matcher.find()) {
            proxies.add(matcher.group(0));
        }
        return proxies;
    }

    private static Set<String> findProxiesInTables(String content) {
        Set<String> proxies = new HashSet<>();
        Matcher matcher = Pattern.compile("<td[^>]*>\\s*(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s*<[^>]*>(\\d+)<\\/td>").matcher(content);
        while (matcher.find()) {
            proxies.add(matcher.group(1) + ":" + matcher.group(2));
        }
        return proxies;
    }

    private static Set<String> findProxiesUsingRegex(String content) {
        Set<String> proxies = new HashSet<>();
        Matcher matcher = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+):(\\d+)").matcher(content);
        while (matcher.find()) {
            proxies.add(matcher.group(1) + ":" + matcher.group(2));
        }
        return proxies;
    }

    private static List<ProxyInfo> testProxiesInBatches(Set<String> proxies, int batchSize, int targetWorkingProxies) {
        List<ProxyInfo> workingProxies = new ArrayList<>();
        String testUrl = "https://www.jotform.com";
        long maxResponseTime = 1200; // 7 seconds threshold

        List<String> proxyList = new ArrayList<>(proxies);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < proxyList.size(); i++) {
            final String proxy = proxyList.get(i);  // Make the proxy final

            tasks.add(() -> {
                if (workingProxies.size() >= targetWorkingProxies) {
                    return null;
                }

                try {
                    // Determine the protocol (socks4 or socks5) and extract IP and port
                    String protocol = proxy.startsWith("socks5://") ? "socks5" : "socks4";
                    String[] parts = proxy.replace("socks4://", "").replace("socks5://", "").split(":");
                    String ip = parts[0];
                    int port = Integer.parseInt(parts[1]);

                    long startTime = System.currentTimeMillis();
                    Proxy proxyObject = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(ip, port));
                    HttpURLConnection connection = (HttpURLConnection) new URL(testUrl).openConnection(proxyObject);
                    connection.setConnectTimeout(3000);
                    connection.setReadTimeout(3000);
                    connection.connect();
                    long responseTime = System.currentTimeMillis() - startTime;

                    // Check if the proxy is fast enough and responsive
                    if (connection.getResponseCode() == 200 && responseTime < maxResponseTime) {
                        ProxyInfo proxyInfo = new ProxyInfo(protocol, ip + ":" + port, responseTime);

                        synchronized (workingProxies) {
                            workingProxies.add(proxyInfo);
                        }

                        // Save working proxy to file in real-time
                        synchronized (PRX.class) {
                            saveWorkingProxiesToFile(Collections.singletonList(proxyInfo), "working_proxies.txt");
                        }

                        System.out.println("Proxy works: " + protocol + "://" + ip + ":" + port + " - " + responseTime + "ms");
                    }

                } catch (Exception ignored) {
                    // Ignore failed proxies
                }
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return workingProxies;
    }

    private static void saveProxiesToFile(Set<String> proxies, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String proxy : proxies) {
                writer.write(proxy);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving proxies to file: " + filename);
        }
    }

    private static void saveWorkingProxiesToFile(List<ProxyInfo> workingProxies, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) { // 'true' to append
            for (ProxyInfo proxy : workingProxies) {
                // Format the proxy in "protocol://IP:Port" format
                String formattedProxy = proxy.getProtocol() + "://" + proxy.getProxy();
                writer.write(formattedProxy);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving working proxies to file: " + filename);
        }
    }


    static class ProxyInfo {
        private final String protocol;
        private final String proxy;
        private final long responseTime;

        public ProxyInfo(String protocol, String proxy, long responseTime) {
            this.protocol = protocol;
            this.proxy = proxy;
            this.responseTime = responseTime;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getProxy() {
            return proxy;
        }

        public long getResponseTime() {
            return responseTime;
        }
    }
}
