<p>&nbsp;</p>
<p></p>
<div class="container">
<div class="header">
<h1>ProxyLander - Proxy Scraping and Testing Tool</h1>
</div>
<p>ProxyLander is a proxy scraping and testing tool designed to collect proxies from multiple sources, verify their functionality, and save the working ones for further use. It allows users to scrape proxy lists from various online sources, test them in batches for functionality and response time, and store the results in real-time. The tool supports SOCKS4 and SOCKS5 proxies, ensuring you have a list of fast and working proxies to use for various applications.</p>
<h2>Features</h2>
<ul>
<li>Scrapes proxy lists from multiple trusted online sources.</li>
<li>Tests the proxies in batches for responsiveness and speed.</li>
<li>Saves working proxies to a file in real time as they are tested.</li>
<li>Automatically sorts working proxies by their response time.</li>
<li>Can handle multiple proxies simultaneously using multithreading.</li>
</ul>
<h2>How to Use</h2>
<ol>
<li><strong>Clone the Repository</strong>: You can clone the repository using the following command:
<pre><code>git clone https://github.com/medaguili/ProxyLander.git
cd ProxyLander</code></pre>
</li>
<li><strong>Compile and Run</strong>: Compile and run the program using your Java IDE or via terminal:
<pre><code>java -jar PRX.jar</code></pre>
</li>
<li><strong>Output</strong>: The program will scrape proxies from the listed URLs and store them in <code>all_proxies.txt</code>. Once the proxies are tested, the working proxies will be saved in <code>working_proxies.txt</code>. You will see updates in real-time on the terminal as proxies are tested and added.</li>
<li><strong>Real-time Updates</strong>: As proxies are found to be working, the file <code>working_proxies.txt</code> will be updated automatically with the new proxies.</li>
</ol>
<h2>Requirements</h2>
<ul>
<li>Java 8 or higher</li>
<li>An active internet connection for proxy scraping</li>
<li>Sufficient system resources for multithreaded proxy testing (e.g., 10 or more threads)</li>
</ul>
<h2>Customizing the Scraping Sources</h2>
<p>The list of proxy URLs to scrape is defined in the <code>URLS</code> constant. You can modify this list to include any other sources you prefer.</p>
<h2>Upcoming Updates</h2>
<ul>
<li>Add proxy authentication support.</li>
<li>Implement a more advanced proxy validation mechanism (e.g., handling DNS leaks).</li>
<li>Improve the user interface for more accessible feedback.</li>
</ul>
<div class="note"><strong>Note:</strong> You can modify the proxy sources or adjust batch sizes according to your needs.</div>
<div class="license">
<p>This project is licensed under the MIT License - see the <a href="LICENSE">LICENSE</a> file for details.</p>
</div>
</div>
<div class="footer">
<p>ProxyLander - Created with ❤️ by Mohammed EL Aguili </p>
</div>
