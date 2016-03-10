package allmondhits;

import java.io.*;

public class AllmondHits {

    public static int count;
       
    public static void main(String[] args) {

        System.out.println("AllmondHits Started...\n");
        
        System.out.println("Usage: java -jar AllmondHits.jar -i diamond.txt -f filterNumber -e evalueFilter(optional) -s bitScoreFilter(optional) -r reads.fastq(optional) -q seqFormat(optional)");
        System.out.println("-i diamond.txt: the original 'diamond view' output file");
        System.out.println("-f filterType OPTIONAL what diamond hits to output: 0=all hits (default), 1=top hits only, 2=top hits and those of equal bitScore");
        System.out.println("-e evalue OPTIONAL filter hits by evalue, only those >= the provided value will be outputted, default=0");
        System.out.println("-s score OPTIONAL filter hits by bit score, only those >= the provided score will be outputted, default=0");
        System.out.println("-r reads.fastq OPTIONAL output reads with no diamond hits: the original reads file that was analysed by diamond");
        System.out.println("-q seqFormat OPTIONAL read sequences are in FASTQ or FASTA format");
        System.out.println("");
        
        String inFilename="";
        String outFilename="";
        
        String readFilename="";
        String readoutFilename="";
        String readhitFilename="";
        
        double evalFil=0, scoreFil=0;
        
        boolean reads=false;
  
        int top=0, fq=4;
        
        if(args.length%2!=0) {
            System.out.println("Odd (uneven) number of parameters - check commad");
            System.exit(0);
        }
        
        for(int i=0;i<args.length-1;i+=2) {
   
                if(args[i].equals("-i")) {//input diamond file
                    inFilename=args[i+1];
                }
                    
                else if(args[i].equals("-f")) {//filter type
                    if(args[i+1].equalsIgnoreCase("0"))//all
                        top=0;
                    else if(args[i+1].equalsIgnoreCase("1"))//just the top
                        top=1;
                    else  if(args[i+1].equalsIgnoreCase("2"))//top plus equal bit score
                        top=2;
                    else {
                        top=0;
                        System.out.println("Unrecognised parameter for filter -f argument = "+args[i+1]+", defaulting to all hits");
                    }
                }
                    
                else if(args[i].equals("-r")) {//reads file
                    readFilename=args[i+1];
                    reads=true;
                }
                    
                else if(args[i].equals("-e")) {//eval filter
                    try { 
                        evalFil=Double.parseDouble(args[i+1]); 
                    }
                    catch (NumberFormatException e) {
                        System.out.println("Eval -e error: argument is not a number "+args[i+1]);
                    }
                }
                    
                else if(args[i].equals("-s")) {//score filter
                    try { 
                        scoreFil=Double.parseDouble(args[i+1]); 
                    }
                    catch (NumberFormatException e) {
                        System.out.println("Score -s error: argument is not a number "+args[i+1]);
                    }
                }
                    
                else if(args[i].equals("-q")) {//filter type
                    if(args[i+1].equalsIgnoreCase("fastq"))
                        fq=4;
                    else if(args[i+1].equalsIgnoreCase("fasta"))
                        fq=2;
                    else {
                        fq=4;
                        System.out.println("Unrecognised parameter for seq format -q argument = "+args[i+1]+", defaulting to FASTQ");
                    }
                }
                    
                else {
                    System.out.println("Unrecognised option: "+args[i]);
                    System.exit(0);
                }
            
    }
        
        /*
        if(args.length==2 | args.length==3) {
            inFilename=args[0];

            if(args[1].equalsIgnoreCase("0"))//all
                top=0;
            else if(args[1].equalsIgnoreCase("1"))//just the top
                top=1;
            else  if(args[1].equalsIgnoreCase("2"))//top plus equal bit score
                top=2;
            //eval filt 
            //bit score filt
            
            if(args.length==3) {
                readFilename=args[2];
                reads=true;
            }
        }
        else {
            System.out.println("Incorrrect Usage - Exiting - but attempting default files");
            inFilename="/Users/rjorton/Downloads/diam243.txt";
            top=1;
            reads=true;
            readFilename="/Users/rjorton/Downloads/PE243.fastq";
        }
        */
        
        
        if(inFilename.indexOf(".")>0) {
            outFilename=inFilename.substring(0, inFilename.lastIndexOf("."))+"_hits.txt";
        }
        else
            outFilename=inFilename+"_hits.txt";
        
        if(reads) {
            if(readFilename.indexOf(".")>0) {
                readoutFilename=readFilename.substring(0, readFilename.lastIndexOf("."))+"_nohit.fastq";
                readhitFilename=readFilename.substring(0, readFilename.lastIndexOf("."))+"_hit.fastq";
            }
            else {
                readoutFilename=readFilename+"_nohit.fastq";
                readhitFilename=readFilename+"_hit.fastq";
            }
        }
        
        System.out.println("Input Diamond File = "+inFilename);
        System.out.println("Output File = "+outFilename);
       
        if(top==0) 
            System.out.println("Outputting all diamond hits");
        else if(top==1) 
            System.out.println("Outputting top diamond hits only");
        else
            System.out.println("Outputting top diamond hits and those with equal bit score");
        
        if(reads) {
            System.out.println("Input Reads File = "+readFilename);
            System.out.println("Output No Hit Reads File = "+readoutFilename);
            System.out.println("Output Hit Reads File = "+readhitFilename);
        }
        else
            System.out.println("No Reads File provided");
        
        System.out.println("Evalue filter <= "+evalFil);
        System.out.println("Bit score filter >= "+scoreFil);
        
        System.out.println("Reading in Diamond hits file");
        
        String prevRead="", read="", nextRead="";
        double eval=0, score=0, prevScore=0;
        int oCount=0, tCount=0, sCount=0, hCount=0, qCount=0, nCount=0, xCount=0, fCount=0;
       
        try{
            FileWriter fstream = new FileWriter(outFilename);
            BufferedWriter out = new BufferedWriter(fstream);
            File inFile = new File(inFilename);
            
            FileWriter readStream = new FileWriter(outFilename);
            BufferedWriter readOut = new BufferedWriter(fstream);
            File readFile = new File(inFilename);
            
            FileWriter readhitStream = new FileWriter(outFilename);
            BufferedWriter readhitOut = new BufferedWriter(fstream);
            
            if(reads) {
                readStream = new FileWriter(readoutFilename);
                readOut = new BufferedWriter(readStream);
                readFile = new File(readFilename);
                
                readhitStream = new FileWriter(readhitFilename);
                readhitOut = new BufferedWriter(readhitStream);
            }

            try {
                BufferedReader input =  new BufferedReader(new FileReader(inFile));
                BufferedReader readInput =  new BufferedReader(new FileReader(inFile));

                if(reads) {
                    readInput =  new BufferedReader(new FileReader(readFile));
                }

                try {
                    String line = null;
                    String readLine= null;

                    while (( line = input.readLine()) != null) {
                        
                        if(count%1000000==0)
                            System.out.println("Currently on Diamond hit "+(count+1));
                        
                        String splits[]=line.split("\t");

                        read=splits[0];
                        eval=Double.parseDouble(splits[10]);
                        score=Double.parseDouble(splits[11]);

                        //System.out.println(eval+" "+evalFil);
                        if(eval>evalFil | score<scoreFil) {
                            if(!read.equalsIgnoreCase(prevRead)) {
                                prevScore=score;
                                fCount++;
                            }
                        }
                        else if(!read.equalsIgnoreCase(prevRead)) {
                            out.write(line+"\n");
                            prevScore=score;
                            oCount++;
                            tCount++;
                        }
                        else if((top==2 & score==prevScore)) {
                            out.write(line+"\n");
                            oCount++;
                            sCount++;
                        }
                        else if(top==0) {
                            out.write(line+"\n");
                            oCount++;
                        }  

                        if(read.equalsIgnoreCase(prevRead) & score>prevScore) {
                            System.out.println("Error?? - prevScore greater than score - not sorted then??");
                            System.out.println(line);
                        }

                        if(reads) {
                            if(!read.equalsIgnoreCase(prevRead)) {
                                
                                readLine = readInput.readLine();
                                
                                if(readLine==null)
                                    break;//break? or continue? if the reads are at the end so should the diamond

                                nextRead=readLine.substring(1, readLine.indexOf(" "));
                                
                                xCount++;
                                qCount=0;

                                while(!read.equalsIgnoreCase(nextRead)) {

                                    readOut.write(readLine+"\n");
                                    readLine = readInput.readLine();
                                    
                                    qCount++;
                                    xCount++;
                                    
                                    if(qCount==fq) {
                                        nextRead=readLine.substring(1, readLine.indexOf(" "));
                                        qCount=0;
                                        nCount++;
                                    }
                                }

                                //if we have exited above - then the read is in diamond
                                //so skip over the next three lines in the FASTQ to the next read
                                qCount=0;
                                readhitOut.write(readLine+"\n");
                                while(qCount<(fq-1)) {
                                    readLine = readInput.readLine();
                                    readhitOut.write(readLine+"\n");
                                    
                                    qCount++;
                                    xCount++;
                                }
                                hCount++;
        
                            }
                        }
                       
                        prevRead=read;
                        count++;
                    }
                    
                    //catch the reads at the end of the read file
                    if(reads) {
                        qCount=0;
                        while (( readLine = readInput.readLine()) != null) {
                            readOut.write(readLine+"\n");

                            qCount++;
                            if(qCount==fq) {
                                nCount++;
                                qCount=0;
                            }
                            xCount++;
                        }
                    }
                        
                  }
                finally {
                    input.close();
                    readInput.close();
                }

            }
            catch (IOException ex) {
                ex.printStackTrace();
            }

            System.out.println(count+" total hits in Diamond file");
            System.out.println(tCount+" top hits outputted");
            System.out.println(fCount+" top hits NOT outputted (evalue and score filters)");
            
            if(top==2)
                System.out.println(sCount+" equal top hits outputted");
            
            System.out.println((oCount-tCount-sCount)+" other hits outputted");
            System.out.println(oCount+" total hits outputted");
            System.out.println(count+" lines in diamond file");
        
            if(reads) {
                System.out.println("Reads with no hits outputted = "+nCount);
                System.out.println("Reads with hits outputted = "+hCount);
                System.out.println("Total reads in file "+(nCount+hCount));
                System.out.println("Total lines in read file "+xCount+" reads check (/4) "+(xCount/4));
            }
            out.close();
            readOut.close();
            readhitOut.close();
  
        }
        
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }

        System.out.println("\n...AllmondHits Finished");
    }
}
