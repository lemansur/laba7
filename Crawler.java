import java.net.*;
import java.util.*;
import java.io.*;

public class Crawler
{
    public static void main(String[] args)
    {
        int depth = 0;
        String URL = "";
        //обрабатывет аргументы командной строки
        if(args.length != 2){
            System.out.println("usage: java Crawler <URL> <depth>");
            System.exit(1);
        }else{
            try{
                depth = Integer.parseInt(args[1]);
            }
            catch(NumberFormatException e){
                System.out.println("usage: java Crawler <URL> <depth>");
                System.exit(1);
            }
            URL = args[0];
        }
        //два списка, в которых хранятся просмотренные и непросмотренные ссылки
        LinkedList<URLDepthPair> reviewedURLs = new LinkedList<URLDepthPair>();
        LinkedList<URLDepthPair> untrackedURLs = new LinkedList<>();
        //новая пара с нулевой глубиной
        URLDepthPair currentDepthPair = new URLDepthPair(URL, 0);
        //добавляет в список непросмотренных
        untrackedURLs.add(currentDepthPair);
        //создает список ссылок, которые уже просматривались
        ArrayList<String> checkedUrls = new ArrayList<String>();
        //добавляет в просмотренные изначальный URL
        checkedUrls.add(currentDepthPair.getURL());
        
        while(untrackedURLs.size() != 0){
            //извлекает пару из непросмотренных
            URLDepthPair depthPair = untrackedURLs.pop();
            //добавляет в список посмотренных
            reviewedURLs.add(depthPair);
            //записывает глубину
            int myDepth = depthPair.getDepth();
            //новый список всех ссылок на странице
            LinkedList<String> urls = new LinkedList<String>();
            //записывает в urls все ссылки
            urls = Crawler.getAllUrl(depthPair);
            String newUrl = "";
            //если не вышли за глубину указанную
            if(myDepth < depth){
                //перебирает все ссылки на странице
                for(int i = 0; i < urls.size(); i++){
                    newUrl = urls.get(i);
                    //если уже есть в просмотренных
                    if(checkedUrls.contains(newUrl)){
                        continue;
                    //иначе
                    }else{
                        //создает новую пару из ссылок с глубиной +1
                        URLDepthPair newDepthPair = new URLDepthPair(newUrl, myDepth + 1);
                        //добавляет в untracked
                        untrackedURLs.add(newDepthPair);
                        //добавляет в просмотренные
                        checkedUrls.add(newUrl);
                    }
                }
            }
        }
        //вывод всех найденных ссылок
        Iterator<URLDepthPair> i = reviewedURLs.iterator();
        while (i.hasNext()) {
            System.out.println(i.next());
        }
    }
    //метод возвращает все ссылки на странице
    private static LinkedList<String> getAllUrl(URLDepthPair depthPair)
    {
        //создает список для всех ссылок
        LinkedList<String> urls = new LinkedList<String>();
        //создает сокет
        Socket sock = null;
        try{
            //создает объект
            sock = new Socket(depthPair.getWebHost(), 80);
        }
        catch(UnknownHostException e){
            System.err.println("UnknownHostException: " + e.getMessage());
            return urls;
        }
        catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            return urls;
        }
        try{
            sock.setSoTimeout(5000);
        }
        catch(SocketException e){
            System.err.println("SocketException: " + e.getMessage());
            return urls;
        }
        //создает поток для общения с сервером
        PrintWriter out = null;
        try{
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);
        }
        catch(IOException e){
            System.err.println("IOException: " + e.getMessage());
            return urls;
        }
        //запрос к серверу
        out.println("GET " + depthPair.getDocPath() + " HTTP/1.1");
        out.println("Host: " + depthPair.getWebHost());
        out.println("Connection: close");
        out.println();
        //еще один поток
        BufferedReader in = null;
        try{
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        }
        catch(IOException e){
            System.err.println("IOException: " + e.getMessage());
            return urls;
        }

        
        
        String document = "";
        //построчно считывает HTML документ
        while(true){
            String line = null;
            try{
                line = in.readLine();
            }
            catch(Exception e){
                System.err.println("IOException: " + e.getMessage());
                System.exit(1);
            }
            if(line == null)
                break;
            document += line;
        }
        
        int beginIndex = 0;
        int endIndex = 0;
        int index = 0;
        //цикл, в котором находятся все вхождения a href=
        while(true){
            String URL_INDICATOR = "a href=\"";
            String END_URL = "\"";
            index = document.indexOf(URL_INDICATOR, index);
            
            if(index == -1)
                break;
            index += URL_INDICATOR.length();
            beginIndex = index;
            endIndex = document.indexOf(END_URL, index);
            index = endIndex;
            //извлекает ссылку
            String newUrl = document.substring(beginIndex, endIndex);
            //проверяет, начинается ли она с http
            if(newUrl.indexOf("http:") != -1){
                //добавляю в список ссылок
                urls.add(newUrl);
            }
        }
        //закрытие сокета
        try{
            sock.close();
        }
        catch(IOException e){
            System.err.println("IOException: " + e.getMessage());
            return urls;
        }
        //возвращает список ссылок
        return urls;
    }
}