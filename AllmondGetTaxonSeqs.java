package allmondgettaxonseqs;

import java.io.*;

/*
 * Aims to retrieve sequences names (e.g. contigs or reads) that are assigned to a particualr taxon in a Krona plot
 * Needs the Krona HTMl file and all the extra files in the ".files" folder
 * Can also go back into the original sequence file and pull out the contigs assigned to that taxon
 * 
 * ToDo:
 * make descend option user defined
 * check if works for FASTQ and how fast
 */

public class AllmondGetTaxonSeqs {

    public static int count;
       
    public static void main(String[] args) {

        String kronaFile="", contigFile="", namesOutFile="", contigsOutFile="";
        String taxon="", taxName="";
        
        boolean getContigs=false;
        boolean descend=true;
        
        String names[] = null;
        
        if(args.length==2) {
            kronaFile=args[0];
            taxon=args[1];
            getContigs=false;
        }
        
        else if(args.length==3) {
            kronaFile=args[0];
            taxon=args[1];
            contigFile=args[2];
            getContigs=true;
        }
        else {

            kronaFile="/Users/richardorton/Downloads/H1_diam_nr.html";
            kronaFile="/Users/richardorton/Downloads/midge1_contigs_diamond_krona.html";
            taxon="10240";//poxviridae
            taxon="10239";//viruses
            //taxon="29263";//LIV
            //taxon=140052;//Beta (Monkey)
            //taxon="0";//root
            
            contigFile="/Users/richardorton/Downloads/midge1_garm_combined.fa";
            getContigs=true;
            
            System.out.println("Incorrect Usage");
            System.out.println("Correct usage: java -jar DiamondGetTaxonContigs.jar kronaHtmlFile taxonID contigsFile(optional)");
            System.out.println("kronaHtmlFile: the HTML file outputed by Krona - the '.files' folder must also be present in the same directory");
            System.out.println("taxonID: the NCBI taxon ID (e.g. '10239' for viruses) - all reads/contigs/seqs assigned to this taxon and any descendent of it will be outputted into a names.txt file");
            System.out.println("contigsFile: this is optional - retrieve all contigs assigned to the taxon from the original file and output them to a contigs.txt file");
            System.out.println("...Exiting");
            
            //System.exit(0);
        }

        namesOutFile=kronaFile.substring(0, kronaFile.lastIndexOf("."))+"_names.txt";
        contigsOutFile=kronaFile.substring(0, kronaFile.lastIndexOf("."))+"_"+taxon+"contigs.txt";
        
        System.out.println("\nProgram AllondGetTaxonSeq Started...\n");
        
        System.out.println("Input Krona file = "+kronaFile);
        System.out.println("Inout Taxon = "+taxon);
        System.out.println("Output names file = "+namesOutFile);
        
        if(getContigs) {
            System.out.println("Input contigs file = "+contigFile);
            System.out.println("Output contigs file = "+contigsOutFile);
        }
        

        //Loop through the krona HTML file and see if the taxonID is there, and find its name
        String filename=kronaFile;
        File inFile = new File(filename);
        
        //Every file has the Root - but Root does not have a taxID - using 0 for that - if 0/Root only want the seqs unassigned at root level
        if(taxon.equalsIgnoreCase("0")) {
            System.out.println("Taxon 0 entered - will be outputting seqs unassigned at the Root level");
            taxName="Root";
            descend=false;
            count++;
        }
        else {
            try {
                BufferedReader input =  new BufferedReader(new FileReader(inFile));

                try {
                    String line = null;
                    String tempN="";

                    while (( line = input.readLine()) != null) {

                        if(line.indexOf("<node name=")>=0) {
                            tempN=line.substring(line.indexOf("<node name=")+12, line.length()-2);
                        }

                        if(line.indexOf("taxon")>=0 & line.indexOf(">"+taxon+"<")>=0) {
                            if(count>0) {
                                System.out.println("ERROR - taxon found multiple times");
                                System.out.println("Exiting");
                                System.exit(0);
                            }

                            taxName=tempN;
                            count++;
                        }
                    }
                  }
                finally {
                    input.close();
                }
            }

            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        
        System.out.println("Taxon "+taxon+" found = "+taxName);
        
        
        if(count==0) {
            System.out.println("Taxon +"+taxon+" not found");
            System.out.println("Exiting");
            System.exit(0);
        }
        
        
        //now loop through the HTMl and pull out all the seq names assgined to that taxon and its children
        count=0;
        int rCount=0, dCount=0;
        
        try{
            FileWriter nameStream = new FileWriter(namesOutFile);
            BufferedWriter namesOut = new BufferedWriter(nameStream);
        
            try {
                BufferedReader input =  new BufferedReader(new FileReader(inFile));

                try {
                    String line = null, prev = null;
                    String tempN="";
                    int nCount=0;
                    boolean test=false;

                    while (( line = input.readLine()) != null) {

                        /*
                         * 
                        //Example of contig names embedded fiectly in HTML
                        
                        <node name="tick-borne encephalitis virus group">
                        <count><val>3</val></count>
                        <score><val>-47.5698</val></score>
                        <rank><val>subgenus</val></rank>
                        <taxon><val href="29263">29263</val></taxon>
                            <node name="Tick-borne encephalitis virus">
                            <rank><val>species</val></rank>
                            <taxon><val href="11084">11084</val></taxon>
                            <members>
                            <vals><val>NODE_43_length_267_cov_1.19811_ID_85</val><val>NODE_155_length_207_cov_1.07895_ID_309</val></vals>
                            </members>
                            <score><val>-38.5158</val></score>
                            <count><val>2</val></count>
                            </node>

                            <node name="Louping ill virus">
                            <count><val>1</val></count>
                            <score><val>-65.6778</val></score>
                            <members>
                            <vals><val>NODE_16_length_367_cov_0.983974_ID_31</val></vals>
                            </members>
                            <rank><val>species</val></rank>
                            <taxon><val href="11086">11086</val></taxon>
                            </node>
                        </node>

                        <node name="tick-borne encephalitis virus group">
                        <count><val>1</val></count>
                        <rank><val>subgenus</val></rank>
                        <members>
                        <vals><val>NODE_1_length_10769_cov_39.7498_ID_1</val></vals>
                        </members>
                        <taxon><val href="29263">29263</val></taxon>
                        <score><val>-450</val></score>
                        </node>
                        </node>

                        //Example of contig names in .js file linked to by HTML
                        
                        <node name="Plasmodium chabaudi">
                         <score><val>-4.28739</val></score>
                         <taxon><val href="5825">5825</val></taxon>
                         <rank><val>species</val></rank>
                         <count><val>8</val></count>
                         <node name="Plasmodium chabaudi chabaudi">
                          <taxon><val href="31271">31271</val></taxon>
                          <members><val>node222.members.0.js</val>
                          </members>
                          <score><val>-4.28739</val></score>
                          <count><val>8</val></count>
                          <rank><val>subspecies</val></rank>
                         </node>
                        </node>
                         */

                        
                        if(line.indexOf("<node name=")>=0) {
                           dCount++;
                        }
                                
                        if(line.indexOf("<node name=\""+taxName+"\">")>=0) {
                           test=true;
                        }

                        if(test) {

                            if(prev.indexOf("<members>")>=0 & line.indexOf("<vals><val>")>=0) {
                                
                                String temp[]=line.substring(line.indexOf("<vals>")+6,line.length()-7).split("</val>");
                                rCount+=temp.length;
                                
                                for(int i=0;i<temp.length;i++) {
                                    namesOut.write(temp[i].substring(5, temp[i].length())+"\n");
                                }
                                
                                if(!descend) {
                                    test=false;
                                }
                            } 
                            else if(line.indexOf("<members><val>")>=0 & line.indexOf(".js")>=0) {

                                String jsName=line.substring(line.indexOf("<val>")+5,line.indexOf("</val>"));

                                File jsFile = new File(kronaFile+".files/"+jsName);

                                try {
                                    BufferedReader jsInput =  new BufferedReader(new FileReader(jsFile));

                                    try {
                                        String jsLine = null;


                                        while (( jsLine = jsInput.readLine()) != null) {
                                            if(jsLine.indexOf("')")>=0) {
                                                continue;
                                            }
                                            if(jsLine.indexOf("data('")>=0) {
                                                namesOut.write(jsLine.substring(jsLine.indexOf("data('")+6, jsLine.indexOf("\\"))+"\n");
                                                rCount++;
                                            }
                                            else {
                                                namesOut.write(jsLine.substring(0, jsLine.indexOf("\\"))+"\n");
                                                rCount++;
                                            }
                                        }
                                      }
                                    finally {
                                        jsInput.close();
                                    }
                                }

                                catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                
                                if(!descend) {
                                    test=false;
                                }
                            }

                            if(line.indexOf("<node name=")>=0)
                                nCount++;
                            else if(line.indexOf("</node>")>=0)
                                nCount--;

                            if(nCount==0)
                                test=false;

                        }

                        prev=line;

                        count++;

                        //if(count%10000==0)
                            //System.out.println("Currently on HTML line "+count);
                    }
                  }
                finally {
                    input.close();
                }
            }

            catch (IOException ex) {
                ex.printStackTrace();
            }

            namesOut.close();
        }

        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
        
        System.out.println("Total lines in HTML file = "+count);
        System.out.println("Total taxon nodes in HTML file = "+dCount);
        System.out.println("Number of reads/contigs assigned to taxon and outputted to _names.txt file = "+rCount);

        
        if(getContigs) {

            if(rCount==0) {
                System.out.println("No reads/contigs were assigned to the taxon");
                System.out.println("Exiting");
                System.exit(0);
            }

            names=new String[rCount];

            for(int i=0;i<names.length;i++) {
                names[i]="";
            }

            //Read in the just outputted _names.txt file
            filename=namesOutFile;
            inFile = new File(filename);
            
            count=0;

            try {
                BufferedReader input =  new BufferedReader(new FileReader(inFile));

                try {
                    String line = null;
                    String tempN="";

                    while (( line = input.readLine()) != null) {
                        names[count]=line;
                        count++;
                    }
                  }
                finally {
                    input.close();
                }
            }

            catch (IOException ex) {
                ex.printStackTrace();
            }
            
            System.out.println("Number of contigs names re-imported = "+count);

            //This is going to be a memory problem if there are a lot of names! and very slow to search in a standard loop
            try {
                FileWriter fstream = new FileWriter(contigsOutFile);
                BufferedWriter contigsOut = new BufferedWriter(fstream);

                filename=contigFile;
                inFile = new File(filename);

                count=0;
                rCount=0;

                try {
                    BufferedReader input =  new BufferedReader(new FileReader(inFile));

                    try {

                        String line = null;
                        boolean test=false;

                        while (( line = input.readLine()) != null) {

                            if(line.indexOf(">")==0) {
                                test=false;
                                rCount++;

                                for(int i=0;i<names.length;i++) {
                                    //this will need to be checked for FASTQ - for how diamond/krona handle spaces and @
                                    if(line.substring(1, line.length()).equalsIgnoreCase(names[i])) {
                                        if(test)
                                            System.out.println("Error - contig found twice - "+names[i]+" "+line);

                                        test=true;
                                        count++;
                                        //I could break here - speed how likeley is the contig name going to be twice - do I want to check?
                                    }
                                }

                                if(test) {
                                    contigsOut.write(line+"\n");
                                }
                            }
                            else {
                                if(test) {
                                    contigsOut.write(line+"\n");
                                }
                            }
                        }
                      }
                    finally {
                        input.close();
                    }
                }

                catch (IOException ex) {
                    ex.printStackTrace();
                }

                System.out.println("Number of contigs found and outputted to _contigs.fasta file = "+count);
                System.out.println("Total number of contigs in the file = "+rCount);

                if(count!=names.length) 
                    System.out.println("Error - did not find all contigs - found "+count+" out of "+names.length);

                contigsOut.close();
            }

            catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error: " + e.getMessage());
            }
        }

        System.out.println("\n...Program Finished");
    }
   
}
