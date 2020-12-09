/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Connection;
import java.io.File;
import java.util.*;
import java.io.FileWriter; 
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
/**
 *
 * @author Chris
 */
public class JavaApplication1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        //getUrls();
        parsePages();
    }
    
    public static void parsePages() throws InterruptedException, IOException
    {
        DatabaseConnection conn = DatabaseConnection.getInstance();
        java.sql.Connection con = conn.connection();
        String statement = "SELECT * FROM `job_database`.`jobs` WHERE `job_processed` = '2'";
        try{
            PreparedStatement st = con.prepareStatement(statement);
            ResultSet rs = st.executeQuery();
            rs.last();    // moves cursor to the last row
            int rs_size = rs.getRow();
            rs.first();
            ArrayList<PreparedStatement> prep_st = new ArrayList<>();
            for(int i = 0; i < rs_size; i++) 
            {
                System.out.println(i);
                try{
                    String url = rs.getString("job_url");
                    int job_id = rs.getInt("job_id");
                    String txt = getText(url).replace("'", "");
                    //System.out.println(txt);
                    if(!txt.equals(""))
                    {
                        String query = "UPDATE `job_database`.`jobs` SET `job_text` = '" + txt + "' WHERE `job_id` = '" + job_id + "'";
                        //System.out.println(query);

                        PreparedStatement st_update = con.prepareStatement(query);
                        st_update.execute();
                        st_update.closeOnCompletion();
                    }
                    else
                    {
                        String query = "UPDATE `job_database`.`jobs` SET `job_processed` = '-2' WHERE `job_id` = '" + job_id + "'";
                        PreparedStatement st_update = con.prepareStatement(query);
                        st_update.execute();
                        st_update.closeOnCompletion();
                    }
                    rs.next();
                }
                catch(Exception e)
                {
                    System.out.println("error occured");
                    System.out.println(rs.getRow());
                    e.printStackTrace();
                }
            }
            st.closeOnCompletion();

        }
        catch(Exception e)
        {
            System.out.println("error occured");
            e.printStackTrace();
        }
    }

    public static String getText(String url) throws InterruptedException    
    {
        String text = "";
        boolean connected = false;
        Document doc = new Document("");
        while(!connected)
        {
            try {

                Connection c = Jsoup.connect(url);
                connected = true;
                boolean gotten = false;
                while(!gotten)
                {
                    try {   
                        doc = c.get();
                        gotten = true;
                    }
                    catch(Exception e) {
                        System.out.println("error occured");
                        e.printStackTrace();
                        Thread.sleep(1000);
                        gotten = false;
                    }
                }
            }
            catch(Exception e)
            {
                System.out.println("error occured");
                e.printStackTrace();
                Thread.sleep(1000);
                connected = false;
            }
        }
        Elements jobText = doc.getElementsByClass("jobDescriptionContent desc");
        if(jobText.size() > 0) {
            text = jobText.get(0).html();
        }
        else
        {
            //System.out.println(url);
        }
        
        
        return text;
    }
    
    public static void getUrls() throws InterruptedException, IOException
    {
        DatabaseConnection conn = DatabaseConnection.getInstance();
        java.sql.Connection con = conn.connection();
               ArrayList<String> urls = new ArrayList<String>();
       ArrayList<String> jobTitles = new ArrayList<String>();
        String site = "https://glassdoor.com";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	Date date = new Date();
	System.out.println(dateFormat.format(date)); //2016/11/16 12:08:43
        new File("C:\\Users\\Chris\\jobsearch\\descriptions\\" + dateFormat.format(date)).mkdir();
        File urlFile = new File("C:\\Users\\Chris\\jobsearch\\urlFiles\\urlFile_" + dateFormat.format(date) + ".txt");
        if(urlFile.createNewFile())
        {
            System.out.println("file created " + urlFile.getName());
        }
        else
        {
            
            System.out.println("file already exists");
            if(urlFile.delete())
            {
                System.out.println("deleted file");
                urlFile.createNewFile(); 
            }
            else
            {
                System.out.println("failed to delete file");
            }
            
        }
        File jobTitleFile = new File("C:\\Users\\Chris\\jobsearch\\jobTitleFiles\\jobTitleFile_" + dateFormat.format(date) + ".txt");
        if(jobTitleFile.createNewFile())
        {
            System.out.println("file created " + jobTitleFile.getName());
        }
        else
        {
            System.out.println("file already exists");
            if(jobTitleFile.delete())
            {
                System.out.println("deleted file");
                jobTitleFile.createNewFile(); 
            }
            else
            {
                System.out.println("failed to delete file");
            }
        }
        String nextPage = "https://www.glassdoor.com/Job/kirkland-software-jobs-SRCH_IL.0,8_IC1150472_KO9,17.htm?fromAge=1&radius=25";
        while(!nextPage.equals(""))
        {
            boolean connected = false;
            Document doc = new Document("");
            while(!connected)
            {
                try {
                    
                    Connection c = Jsoup.connect(nextPage);
                    connected = true;
                    boolean gotten = false;
                    while(!gotten)
                    {
                        try {   
                            doc = c.get();
                            gotten = true;
                        }
                        catch(Exception e) {
                            System.out.println("error occured");
                            e.printStackTrace();
                            Thread.sleep(5000);
                            gotten = false;
                        }
                    }
                }
                catch(Exception e)
                {
                    System.out.println("error occured");
                    e.printStackTrace();
                    Thread.sleep(5000);
                    connected = false;
                }
            }

        // TODO code application logic here
        
       // System.out.println(doc.body().text());

       Elements test = doc.getElementsByClass("jobLink jobInfoItem jobTitle ");
       int counter = 0;
       for(Element e: test)
       {
           if(counter % 2 == 1)
           {
            System.out.println(e.text());
            System.out.println(site + e.attr("href"));
            urls.add(site + e.attr("href"));
            jobTitles.add(e.text());
           }
            counter++;
       }
       Elements pageControls = doc.getElementsByClass("pagingControls cell middle");
       
       if(pageControls.size() > 0)
       {
           Element page = pageControls.get(0);
           Elements next = page.getElementsByClass("next");
           if(next.size() > 0)
           {
                nextPage = site + next.get(0).child(0).attr("href");
                System.out.println(nextPage);
           }
           else
           {
               nextPage = "";
           }
       }
       else
       {
           System.out.println("page controls no size");
       }
       
       if(nextPage.equals("https://glassdoor.com"))
       {
           nextPage = "";
       }

       Thread.sleep(5000);
      }
        try{
        FileWriter urlWriter = new FileWriter(urlFile);
        FileWriter jobTitleWriter = new FileWriter(jobTitleFile);

        if(urls.size() == jobTitles.size())
        {
            for(int i = 0; i < urls.size(); i++)
            {
                String statement = "INSERT INTO `job_database`.`jobs` (`job_title`, `job_url`, `job_date`) VALUES ('" + jobTitles.get(i) + "','" + urls.get(i) + "','" + dateFormat.format(date) +"')";
                System.out.println(statement);
                PreparedStatement ps = con.prepareStatement(statement);
                int status = ps.executeUpdate();
                if(status == 0)
                {
                    System.out.println("faield");
                }
                urlWriter.write(urls.get(i)+"\n");
                
                jobTitleWriter.write(jobTitles.get(i)+"\n");
            }
            urlWriter.flush();
            jobTitleWriter.flush();
        }
        else
        {
            System.out.println("numebr of urls and job titles does not match up");
        }
        urlWriter.close();
        jobTitleWriter.close();
        }
        catch(Exception e)
        {
            System.out.println("error occured");
            e.printStackTrace();
        }
    }
    
    public static void getDescriptions() throws InterruptedException, IOException
    {
       DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	Date date = new Date();
	System.out.println(dateFormat.format(date));
        ArrayList<String> urls = new ArrayList<String>();
        Scanner scan = new Scanner(new File("C:\\Users\\Chris\\jobsearch\\urlFiles\\urlFile_" + dateFormat.format(date) + ".txt"));
        while(scan.hasNextLine())
        {
            urls.add(scan.nextLine());
        }
        
        ArrayList<String> jobDescriptions = new ArrayList<String>();
        for(int i = 0; i < urls.size(); i++)
        {

            String url = urls.get(i);
            boolean connected = false;
            Document doc = new Document("");
            while(!connected)
            {
                try {
                    
                    Connection c = Jsoup.connect(url);
                    connected = true;
                    boolean gotten = false;
                    while(!gotten)
                    {
                        try {   
                            doc = c.get();
                            gotten = true;
                        }
                        catch(Exception e) {
                            System.out.println("error occured");
                            e.printStackTrace();
                            Thread.sleep(5000);
                            gotten = false;
                        }
                    }
                }
                catch(Exception e)
                {
                    System.out.println("error occured");
                    e.printStackTrace();
                    Thread.sleep(5000);
                    connected = false;
                }
            }
            Elements descriptions = doc.getElementsByClass("jobDescriptionContent desc");
            if(descriptions.size() > 0)
            {
                jobDescriptions.add(descriptions.text());
                System.out.println(descriptions.text());
                File desc = new File("C:\\Users\\Chris\\jobsearch\\descriptions\\" + dateFormat.format(date)  + "\\desc_" + i + ".txt");
                if(desc.createNewFile())
                {
                    System.out.println("created file " + desc.getName());
                }
                else
                {
                     System.out.println("file already exists");
                    if(desc.delete())
                    {
                        System.out.println("deleted file");
                        desc.createNewFile(); 
                    }
                    else
                    {
                        System.out.println("failed to delete file");
                    }
                }
                FileWriter descWriter = new FileWriter(desc);
                descWriter.write(descriptions.text());
                descWriter.flush();
                descWriter.close();
            }
            else
            {
                System.out.println("no job descrption");
            }
        }
    }
}
