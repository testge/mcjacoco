package com.example.mc_jacoco.template;

/**
 * @author luping
 * @date 2023/11/3 00:24
 */
public class CovIndexHtmlTemPlate {

    public static String HTML_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">\n" +
            "<head>\n" +
            "    <meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\" />\n" +
            "    <link rel=\"stylesheet\" href=\"jacoco-resources/report.css\" type=\"text/css\" />\n" +
            "    <link rel=\"shortcut icon\" href=\"jacoco-resources/report.gif\" type=\"image/gif\" />\n" +
            "    <title>manualDiffCoverageReport</title>\n" +
            "    <script type=\"text/javascript\" src=\"jacoco-resources/sort.js\"></script>\n" +
            "</head>\n" +
            "<body onload=\"initialSort(['breadcrumb', 'coveragetable'])\">\n" +
            "    <div class=\"breadcrumb\" id=\"breadcrumb\"><span class=\"info\"><a href=\"jacoco-sessions.html\" class=\"el_session\">Sessions</a></span><span class=\"el_report\">manualDiffCoverageReport</span></div>\n" +
            "    <h1>manualDiffCoverageReport</h1>\n" +
            "    <table class=\"coverage\" cellspacing=\"0\" id=\"coveragetable\">\n" +
            "        <thead>\n" +
            "            <tr>\n" +
            "                <td class=\"sortable\" id=\"a\" onclick=\"toggleSort(this)\">Element</td>\n" +
            "                <td class=\"down sortable bar\" id=\"b\" onclick=\"toggleSort(this)\">Missed Instructions</td>\n" +
            "                <td class=\"sortable ctr2\" id=\"c\" onclick=\"toggleSort(this)\">Cov.</td>\n" +
            "                <td class=\"sortable bar\" id=\"d\" onclick=\"toggleSort(this)\">Missed Branches</td>\n" +
            "                <td class=\"sortable ctr2\" id=\"e\" onclick=\"toggleSort(this)\">Cov.</td>\n" +
            "                <td class=\"sortable ctr1\" id=\"f\" onclick=\"toggleSort(this)\">Missed</td>\n" +
            "                <td class=\"sortable ctr2\" id=\"g\" onclick=\"toggleSort(this)\">Cxty</td>\n" +
            "                <td class=\"sortable ctr1\" id=\"h\" onclick=\"toggleSort(this)\">Missed</td>\n" +
            "                <td class=\"sortable ctr2\" id=\"i\" onclick=\"toggleSort(this)\">Lines</td>\n" +
            "                <td class=\"sortable ctr1\" id=\"j\" onclick=\"toggleSort(this)\">Missed</td>\n" +
            "                <td class=\"sortable ctr2\" id=\"k\" onclick=\"toggleSort(this)\">Methods</td>\n" +
            "                <td class=\"sortable ctr1\" id=\"l\" onclick=\"toggleSort(this)\">Missed</td>\n" +
            "                <td class=\"sortable ctr2\" id=\"m\" onclick=\"toggleSort(this)\">Classes</td>\n" +
            "            </tr>\n" +
            "        </thead>\n" +
            "        <tbody>\n" +
            "        </tbody>\n" +
            "<tfoot></tfoot>" +
            "    </table>\n" +
            "    <div class=\"footer\"><span class=\"right\">Created with <a href=\"http://www.jacoco.org/jacoco\">JaCoCo</a> 1.0.1.201909190214</span></div>\n" +
            "</body>\n" +
            "</html>";
}
