package allmondcoverage;

import java.io.*;

//need to set the -Xms argument for more memory

public class AllmondCoverage {
    
    public static int count;
    public static int cov[]=new int[100000];//the ref file is protein - so the max length shouldn't be a problem
    
    public static String data[][];
    public static int len[][];
    
    public static String virus="", prot="", acc="", tax="", gi="", geno="", gene="", gStart="";
    public static int vCount=0, vLen=0, prevGI=0,giNum=0;
          
    
    public static void main(String[] args) {
        
        String inFilename="", refFilename="", outFilename="", covFilename="";

        if(args.length==2) {
            inFilename=args[0];
            refFilename=args[1];
        }
        else {
            inFilename="/Users/richardorton/Downloads/diam243_top_sort2.txt";
            refFilename="/Users/richardorton/Downloads/diamond_all_viral_protein_out_sort.txt";
        }
        
        outFilename=inFilename.substring(0, inFilename.indexOf("."))+"_out.txt";
        covFilename=inFilename.substring(0, inFilename.indexOf("."))+"_cov.txt";
        
        System.out.println("\nProgram AllmondCoverage Started...");
 
        System.out.println("Input diamond file = "+inFilename);
        System.out.println("Input reference file = "+refFilename);
        System.out.println("Output coverage file = "+outFilename);
        
        System.out.println("Reading in Diamond reference file");
        
        File inFile = new File(refFilename);
        
        try {
            BufferedReader input =  new BufferedReader(new FileReader(inFile));
            
            try {
                String line = null;
                
                while (( line = input.readLine()) != null) {
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
        
        System.out.println(count+" sequences in file");
        
        
        data=new String[count][8];
        len=new int[count][2];
        count=0;
        
        
        try {
            BufferedReader input =  new BufferedReader(new FileReader(inFile));
            
            try {
                String line = null;
                
                
                while (( line = input.readLine()) != null) {
                    
                    String splits[]=line.split("\t");
                    
                    data[count][0]=splits[0];//gi
                    data[count][1]=splits[1];//accession
                    data[count][2]=splits[2];//protein
                    data[count][3]=splits[3];//virus
                    data[count][4]=splits[4];//taxid
                    data[count][5]=splits[9];//genome AccessionID
                    data[count][6]=splits[10];//geneName
                    data[count][7]=splits[12];//geneStart
                    
                    len[count][0]=Integer.parseInt(splits[5]);//length AA
                    len[count][1]=Integer.parseInt(splits[6]);//length NT
                    
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
        
        //the dimaond data should be sorted by GI
        for(int i=1;i<data.length;i++) {
            if(Integer.parseInt(data[i][0])<Integer.parseInt(data[i-1][0]))
                System.out.println("Error - not sorted - "+i+" "+data[i][0]+" "+data[i-1][0]);
        }
        
        
        System.out.println("Opening output file");
        
        try{
            FileWriter fstream = new FileWriter(outFilename);
            BufferedWriter out = new BufferedWriter(fstream);
            
            FileWriter covStream = new FileWriter(covFilename);
            BufferedWriter covOut = new BufferedWriter(covStream);
            
            out.write("GI\tVirus\tTaxID\tProtein\tProtAccession\tProtLength\tReads\tCovAvCov\tRealAvCov\tCov0\tCov1\tCov5\tCov10\tCov50\tCov100\tCovSum\tAASum\tGenome\tGene\tGeneStart\n");
            
            System.out.println("Reading in Diamond hits file");
            
            inFile = new File(inFilename);

            int fCount=0, ind=0;
            count=0;
            
            
            try {
                BufferedReader input =  new BufferedReader(new FileReader(inFile));
                
                try {
                    String line = null;
                    
                    while (( line = input.readLine()) != null) {
                        
                        String splits[]=line.split("\t");
                        String gis[]=splits[1].split("\\|");
                        
                        giNum=Integer.parseInt(gis[1]);
                        gi=gis[1];
                        
                        int start=Integer.parseInt(splits[8])-1;
                        int end=Integer.parseInt(splits[9])-1;
                        
                        boolean found=false;
                        
                        if(count%100000==0)
                          System.out.println("Currently on diamond hit = "+count);
                        
                        for(int i=ind;i<data.length;i++) {
       
                            if(data[i][0].equalsIgnoreCase(gi)) {
                                ind=i-1;
                                //System.out.println(gi+" "+line);

                                if(giNum!=prevGI) {
                                    String thisOut[]=nextProt(i);
                                    covOut.write(thisOut[0]);
                                    out.write(thisOut[1]);
                                }
                                
                                if(end>=cov.length) {
                                    System.out.println("Error - the diamond hit goes beyond the reference end for GI = "+gi+" diamond end = "+(end+1)+" ref end = "+cov.length+" - it will be trimmed");
                                    end=cov.length-1;
                                }
                                
                                for(int j=start;j<=end;j++)
                                    cov[j]++;
                                
                                vCount++;
                                found=true;
                                fCount++;
                                
                                break;
                            }
                        }
                        
                        if(!found) {
                            System.out.println("Error - could not find GI - "+gi+" "+line);
                        }
                        
                        prevGI=giNum;
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
            
            //catch the last
            String thisOut[]=nextProt(data.length-1);
            covOut.write(thisOut[0]);
            out.write(thisOut[1]);
            

            System.out.println(count+" seqs in file, found = "+fCount);
            
            out.close();
            
        }
        
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
        
        
        System.out.println("...Program Finished");
    }
    
    private static String[] nextProt(int thisI) {
        
        String thisOut[]=new String[2];
        thisOut[0]="";//the coverage
        thisOut[1]="";//the hit summary
        
        double a=0,b=0,c0=0,c1=0,c5=0,c10=0,c50=0,c100=0;
        double avCov=0, realCov=0;
        
        thisOut[0]=">"+prevGI+"|"+virus+"|"+tax+"|"+prot+"|"+acc+"|"+geno+"|"+gene+"\n";

        for(int j=0;j<cov.length;j++) {
            
            if(cov[j]>0) {
                a++;
                b+=cov[j];
            }

            if(cov[j]==0)
                c0++;
            else if (cov[j]>0)
                c1++;
            if(cov[j]>=5)
                c5++;
            if(cov[j]>=10)
                c10++;
            if(cov[j]>=50)
                c50++;
            if(cov[j]>=100)
                c100++;

            int phredCov=cov[j];

            //if(phredCov>10000)
                //phredCov=10000;

            if(phredCov==0) {
                thisOut[0]+="!";
            }
            else if(phredCov==1) {
                thisOut[0]+="\"";
            }
            else {
                double phredP=(double)1/(double)phredCov;
                double phredQ=-10*Math.log10(phredP)+0.5;//+0.5 for rounding
                char phredC=(char)((int)phredQ+33);
                thisOut[0]+=phredC;
            }

        }
        thisOut[0]+="\n";

        avCov=b/a;
        realCov=b/cov.length;

        c0=c0/cov.length*100;
        c1=c1/cov.length*100;
        c5=c5/cov.length*100;
        c10=c10/cov.length*100;
        c50=c50/cov.length*100;
        c100=c100/cov.length*100;

        if(count>0)
            thisOut[1]=prevGI+"\t"+virus+"\t"+tax+"\t"+prot+"\t"+acc+"\t"+vLen+"\t"+vCount+"\t"+avCov+"\t"+realCov+"\t"+c0+"\t"+c1+"\t"+c5+"\t"+c10+"\t"+c50+"\t"+c100+"\t"+b+"\t"+a+"\t"+geno+"\t"+gene+"\t"+gStart+"\n";
        else
            thisOut[0]="";

        vLen=len[thisI][0];
        cov=new int[vLen];//had a +1 here but removed - not needed

        acc=data[thisI][1];
        prot=data[thisI][2];
        virus=data[thisI][3];
        tax=data[thisI][4];
        geno=data[thisI][5];
        gene=data[thisI][6];
        gStart=data[thisI][7];

        vCount=0;
        //System.out.println("New species = "+data[i][0]+" "+len[i][0]);
        
        return thisOut;
    }
    
    
}
