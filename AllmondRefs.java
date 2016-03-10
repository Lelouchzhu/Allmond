package allmondrefs;

import java.io.*;
import java.util.*;


public class AllmondRefs {

    public static int count;
       
    public static void main(String[] args) {


        String inFilename="", taxFilename="",accFilename="", outFilename="", outGenFilename="", outTaxFilename="";
        
        
        if(args.length==1) {
            inFilename=args[0];
        }
        else {
            inFilename="/Users/richardorton/Downloads/diamond_all_viral_protein.fa";
            taxFilename="/Users/richardorton/Downloads/diamond_gis2taxid.txt";
            accFilename="/Users/richardorton/Downloads/gene2accession_sorted.txt";
            
            //diamond_all_viral_protein.fa is the diamond protein seqs that go into the diamon db - in this case the refseq only proteins seq
            //extract the gis from the protein seq file:  awk -F '\t' '{print $1}' diamond_all_viral_protein_out.txt > diamond_gis_only.txt 
            //get the taxids for the gis: /software/ncbi_toolkit_12/bin/gi2taxid -file diamond_gis_only.txt > diamond_gis2taxid.txt
            
            //get the gene2accession.gz file: https://www.biostars.org/p/145216/#147578
            //sort by by taxonID then protID: (head -n 1 gene2accession.txt && tail -n +2 gene2accession.txt | sort -k 1,1 -k 7,7 -n ) > gene2accession_sorted.txt
            
        }

        
        outFilename=inFilename.substring(0, inFilename.indexOf("."))+"_out.txt";
        outGenFilename=inFilename.substring(0, inFilename.indexOf("."))+"_gen.txt";
        outTaxFilename=inFilename.substring(0, inFilename.indexOf("."))+"_tax.txt";
        
        System.out.println("Program DiamondRefs Started...");
        
        System.out.println("Sequence File = "+inFilename);
        System.out.println("Taxon File = "+taxFilename);
        System.out.println("Accession File = "+accFilename);
        System.out.println("Output File = "+outFilename);
        
        System.out.println("Reading in seq file "+inFilename);
        File inFile = new File(inFilename);
        
        try {
            BufferedReader input =  new BufferedReader(new FileReader(inFile));

            try {
                String line = null;

                while (( line = input.readLine()) != null) {
                    if(line.indexOf(">")==0) {
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
        
        System.out.println(count+" seqs in file");
        
        String data[][]=new String[count][5];//protAcc, protName, speciesName, genomeAcc, geneName
        int nums[][]=new int[count][12];//count, protGI, lenAA, lenNT, taxID, combAAlen, combNtlen, genomeGI, genomeFrom, GenomTo
        
        count=-1;

        try {
            BufferedReader input =  new BufferedReader(new FileReader(inFile));

            try {
                String line = null;


                while (( line = input.readLine()) != null) {

                    if(line.indexOf(">")==0) {
                        count++;

                        String splits[]=line.split("\\|");
                        
                        nums[count][0]=count;//used for sorting back to original order
                        nums[count][1]=Integer.parseInt(splits[1]);//prot gi

                        data[count][0]=splits[3];//accession prot
                        data[count][3]="not found";//genome accession - a couple missing in gene2accession, so flagging them
                        data[count][4]="not found";
                        
                        String temp="";
                        
                        if(splits[4].indexOf("[")<0)
                            temp=splits[5];
                        else
                            temp=splits[4];
      
                        data[count][1]=temp.substring(0, temp.lastIndexOf("["));
                        data[count][2]=temp.substring(temp.lastIndexOf("[")+1, temp.lastIndexOf("]"));
                    }
                    else {
                        nums[count][2]+=line.length();//prot length
                        nums[count][3]=nums[count][2]*3;//gene length
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
        

        System.out.println("Reading in gi2taxid file "+taxFilename);
        inFile = new File(taxFilename);
        count=0;
        
        try {
            BufferedReader input =  new BufferedReader(new FileReader(inFile));

            try {
                String line = null;


                while (( line = input.readLine()) != null) {

                    String splits[]=line.split(" ");

                    if(Integer.parseInt(splits[0])!=nums[count][1])
                        System.out.println("Error - gis not equal - "+count+" "+splits[0]+" "+data[count][0]);
                    
                    nums[count][4]=Integer.parseInt(splits[1]);//taxid
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
        
        System.out.println(count+" gi2taxids in file");
        
        if(count!=nums.length)
            System.out.println("Error - not enough tax ids in file");

        System.out.println("Chomping");
        
        for(int i=0;i<data.length;i++) {

            while(data[i][1].length()>0 & data[i][1].charAt(0)==' ')
                data[i][1]=data[i][1].substring(1, data[i][1].length());
            
            while(data[i][1].length()>0 & data[i][1].charAt(data[i][1].length()-1)==' ')
                data[i][1]=data[i][1].substring(0, data[i][1].length()-1);
        }

        
        System.out.println("Checking for duplictes - and summing taxon lengths");
        
        for(int i=0;i<data.length;i++) {
  
            if(i%10000==0)
                System.out.println("DupLoop "+i);
            
            nums[i][5]+=nums[i][2];
                       
            for(int j=i+1;j<data.length;j++) {
                
                if(nums[i][4]==nums[j][4]) {
                    nums[i][5]+=nums[j][2];
                    nums[j][5]+=nums[i][2];
                }
                
                if(nums[i][1]==nums[j][1]) {
                    System.out.println("Error - duplictaes - "+i+" "+j+" "+nums[i][1]+" "+nums[j][1]);//check the prot GIs
                }
                
                if(data[i][0].equalsIgnoreCase(data[j][0])) {
                    System.out.println("Error - duplictaes - "+i+" "+j+" "+data[i][10]+" "+data[j][0]);//check the prot Accessions
                }
            }
            
            nums[i][6]=nums[i][5]*3;
        }
        
        
        System.out.println("Sorting data");
        //Sort the data by taxon, then protGi so in same sort order as gene2accession file
        
        Arrays.sort(nums, new Comparator() {
            public int compare(Object o1, Object o2) {
                int[] a = (int[])o1;
                int[] b = (int[])o2;
                
                if(a[4]==b[4])
                   return Integer.valueOf(a[1]).compareTo(b[1]);//if the same taxon, then sub-sort by protGI 
                else
                    return Integer.valueOf(a[4]).compareTo(b[4]);//if different taxons, sort by taxon
            }
            public boolean equals(Object o) {
                return this == o;
            }
        });
        
        System.out.println("Checking sort");
        
        for(int i=1;i<nums.length;i++) {
            if(nums[i][4]<nums[i-1][4])
                System.out.println("erorr - taxids not sorted");
            
            else if(nums[i][4]==nums[i-1][4] & nums[i][1]<nums[i-1][1])
                System.out.println("erorr - protGIs not sorted");
        }

        
        System.out.println("Reading in gene2accession file");//file already sorted by taxon and protGI
        inFile = new File(accFilename);
        
        count=-3;//two header lines in file
        int found=0, curr=0, prevTax=0, prevProt=0;
        
        try {
            BufferedReader input =  new BufferedReader(new FileReader(inFile));

            try {
                String line = null;


                while (( line = input.readLine()) != null) {
                    count++;

                    if(count<0)//skip header
                        continue;
                    
                    if(count%10000000==0)
                        System.out.println("Gene Loop: "+count);

                    String splits[]=line.split("\t");

                    int taxID=Integer.parseInt(splits[0]);
                    int protID=0;

                    if(!splits[6].equalsIgnoreCase("-"))
                        protID=Integer.parseInt(splits[6]);

                    if(taxID<prevTax)
                        System.out.println("Error - taxIDs not sorted");
                    else if(taxID==prevTax & protID<prevProt)
                        System.out.println("Error - protIDs not sorted");

                    while(taxID>nums[curr][4]) {
                        curr++;
                        //System.out.println("not found "+nums[curr][4]+" "+nums[curr][1]+" "+curr);
                    }

                    //there is an issue with start coordnate - a protein will be in multiple times in the gene2accesion file 
                    //join(3981..5386,1..136)
                    //374840	2546398	PROVISIONAL	-	-	NP_040703.1	9626373	NC_001422.1	9626372	0	135	+	-	-	-	A
                    //374840	2546398	PROVISIONAL	-	-	NP_040703.1	9626373	NC_001422.1	9626372	3980	5385	+	-	-	-	A
                    //if the prot is from a join - looks like separte entry for each component of the join

                    if(taxID==nums[curr][4] & protID==nums[curr][1]) {
                        data[nums[curr][0]][3]=splits[7];//data has not been sorted so usd original location stored in [0]
                        data[nums[curr][0]][4]=splits[15];
                        
                        nums[curr][7]=Integer.parseInt(splits[8]);//genome GI
                        nums[curr][8]=Integer.parseInt(splits[9]);//gene Start
                        nums[curr][9]=Integer.parseInt(splits[10]);//gene Stop
                        
                        found++;
                        curr++;
                    }

                    if(curr>=nums.length) {
                        System.out.println("Lasted array entry reached at: "+count);
                        break;
                    }

                    prevTax=taxID;
                    prevProt=protID;
                }
              }
            finally {
                input.close();
            }
        }

        catch (IOException ex) {
            ex.printStackTrace();
        }
        
        
        System.out.println("Read in "+count+" lines");
        System.out.println("Matched "+found+" seqs");
        
        
        System.out.println("Resorting on genome GI");
        
        Arrays.sort(nums, new Comparator() {
            public int compare(Object o1, Object o2) {
                int[] a = (int[])o1;
                int[] b = (int[])o2;
                
                return Integer.valueOf(a[7]).compareTo(b[7]);
            }
            public boolean equals(Object o) {
                return this == o;
            }
        });
        
        count=0;
        for(int i=0;i<nums.length;i++) {
            if(nums[i][7]<=0) {
                count++;
                continue;//should be the not founds
            }
            
            nums[i][10]+=nums[i][2];
                
            for(int j=i+1;j<nums.length;j++) {
                if(nums[i][7]!=nums[j][7])
                    break;
                
                nums[i][10]+=nums[j][2];
                nums[j][10]+=nums[i][2];
            }
            
            nums[i][11]=nums[i][10]*3;
        }
        
        System.out.println(count+" missing genome GIs");
        
        System.out.println("Sorting data back");
        
        Arrays.sort(nums, new Comparator() {
            public int compare(Object o1, Object o2) {
                int[] a = (int[])o1;
                int[] b = (int[])o2;
                
                return Integer.valueOf(a[0]).compareTo(b[0]);
            }
            public boolean equals(Object o) {
                return this == o;
            }
        });
                
        System.out.println("Outputting file");
        
        try{
            FileWriter fstream = new FileWriter(outFilename);
            BufferedWriter out = new BufferedWriter(fstream);
            
            for(int i=0;i<data.length;i++) {

                out.write(nums[i][1]+"\t");//protGI
                out.write(data[i][0]+"\t");//protAcc
                out.write(data[i][1]+"\t");//geneName
                out.write(data[i][2]+"\t");//speciesName
                out.write(nums[i][4]+"\t");//taxID
                out.write(nums[i][2]+"\t");//lenAA
                out.write(nums[i][3]+"\t");//lenNt
                out.write(nums[i][5]+"\t");//taxAA
                out.write(nums[i][6]+"\t");//taxNt
                out.write(data[i][3]+"\t");//genAcc
                out.write(data[i][4]+"\t");//geneName
                out.write(nums[i][7]+"\t");//genGI
                out.write(nums[i][8]+"\t");//startGen
                out.write(nums[i][9]+"\t");//stopGen
                out.write(nums[i][10]+"\t");//genomeAA
                out.write(nums[i][11]+"\t");//genomeNt
                out.write(nums[i][0]+"\t");//orderNum
                out.write("\n");
            }
        
            out.close();
  
        }

        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
        
        
        
        

        try{
            FileWriter fstreamGen = new FileWriter(outGenFilename);
            BufferedWriter outGen = new BufferedWriter(fstreamGen);
            
            FileWriter fstreamTax = new FileWriter(outTaxFilename);
            BufferedWriter outTax = new BufferedWriter(fstreamTax);
            
            String prevGen="";
            prevTax=0;
            
            int gCount=0;
            int tCount=0;
            
            outGen.write("TaxID\tSpeciesName\tGenomeAssession\tProteins\n");
            
            int gens=0, gProts=0, tProts=0;
            
            for(int i=0;i<data.length;i++) {

                if(!data[i][3].equalsIgnoreCase(prevGen)) {

                    if(gCount>0) {
                        outGen.write(gProts+"\n");
                        gProts=0;
                    }
                    
                    //TaxID-SpeciesName-GenomeAssession
                     outGen.write(nums[i][4]+"\t"+data[i][2]+"\t"+data[i][3]+"\t");
                    
                    gCount++;
                }

                //ProtGI-ProtAcc-LenAA
                outGen.write(nums[i][1]+"$"+data[i][0]+"$"+nums[i][2]+"\t");
                gProts++;
                

                if(nums[i][4]!=prevTax) {

                    if(tCount>0) {
                        outTax.write(tProts+"\t"+gens+"\n");
                        tProts=0;
                        gens=0;
                    }
                    
                    if(!data[i][3].equalsIgnoreCase(prevGen))
                        gens++;
                    
                    //TaxID-SpeciesName
                    outTax.write(nums[i][4]+"\t"+data[i][2]+"\t");
                    
                    tCount++;
                }

                //GenAcc-ProtGI-ProtAcc-LenAA
                outTax.write(data[i][3]+"$"+nums[i][1]+"$"+data[i][0]+"$"+nums[i][2]+"\t");
                tProts++;
                
                prevGen=data[i][3];
                prevTax=nums[i][4];
            }
        
            outGen.write(gProts+"\n");
            outTax.write(tProts+"\t"+gens+"\n");
            outGen.close();
            outTax.close();
  
        }

        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
        
        //Read output back in
        //For tax - sort by tax ID - taxID should be unique
        //For genome - sort by Genome accession - should be unique
        
        //Will highliht things that will have to be manually changed!!!!
        
        
      
        System.out.println("...Program Finished");
        
        
    }
   
}
