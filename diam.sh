echo "DIAMOND-VIRAL REFSEQ"

#Simple cat rather than interleave
cat $1_clean_1.fastq $1_clean_2.fastq > $1_clean.fastq

#Diamond BLAST
mkdir -p diam_temp
diamond blastx -d /home2/db/diamond/ViralRefSeqProtein -f 6 -o $1_diam_ref.txt -p 10 -q $1_clean.fastq -t diam_temp 
ktImportBLAST $1_diam_ref.txt -o $1_diam_ref.html > $1_kt_ref.txt

java -jar ~orto01r/dist/AllmondFilterBlastHits.jar -i $1_diam_ref.txt -f 1 

#Allmond Coverage
echo "ALLMOND-COVERAGE"
#This needs to be updated in the future - currently uses GIs
sort -t '|' -k 2,2 -n $1_diam_ref_hits.txt > $1_diam_ref_hits_sort.txt
java -jar ~orto01r/dist/AllmondCoverage.jar $1_diam_ref_hits_sort.txt ~orto01r/dist/diamond_all_viral_protein_out_sort.txt > allmond_output.txt 

rm -f $1_clean.fastq
#rm -f $1_diam_ref.txt
rm -f $1_diam_ref_hits.txt
#rm -f $1_diam_ref_hits_sort.txt
rm -f $1_diam_ref_hits_sort_cov.txt

