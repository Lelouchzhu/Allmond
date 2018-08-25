echo "SPADES-CLEANED"
spades.py --only-assembler -t 10 -1 $1_clean_1.fastq -2 $1_clean_2.fastq -o ./spades_fil
sort_contigs -z -b spades_fil/contigs.fasta spades_fil/contigs_sort.fasta

echo "contigs" >> $1_log.txt
expr `(grep "^>" spades_fil/contigs.fasta | wc -l |cut -f1 -d " ")` >> $1_log.txt

cd spades_fil/
echo "DIAMOND-CONTIGS"
mkdir -p diam_temp
diamond blastx -d /home2/db/diamond/nr -f 6 -o $1_diam_nr.txt -p 10 -q contigs_sort.fasta -t diam_temp
ktImportBLAST $1_diam_nr.txt -o $1_diam_nr.html > $1_kt.txt

diamond blastx -d /home2/db/diamond/ViralRefSeqProtein -f 6 -o $1_diam_ref.txt -p 10 -q contigs_sort.fasta -t diam_temp 
ktImportBLAST $1_diam_ref.txt -o $1_diam_ref.html > $1_kt_ref.txt

java -jar ~orto01r/dist/AllmondFilterBlastHits.jar -i $1_diam_ref.txt -f 1 

sort -t '|' -k 2,2 -n $1_diam_ref_hits.txt > $1_diam_ref_hits_sort.txt
java -jar ~orto01r/dist/AllmondCoverage.jar $1_diam_ref_hits_sort.txt ~orto01r/dist/diamond_all_viral_protein_out_sort.txt > allmond_output.txt

java -jar ~orto01r/dist/ortools.jar -m contigs.fasta 1
#java -jar ~orto01r/dist/AllmondHits.jar -i $1_diam_ref.txt -r contigs_new.fasta -q FASTA
java -jar ~orto01r/dist/AllmondHits.jar -i $1_diam_nr.txt -r contigs_new.fasta -q FASTA

getorf -sequence contigs_new_nohit.fasta -outseq contigs_new_nohit_orfs.fasta -find 1 -minsize 90
sort_contigs -z -b contigs_new_nohit_orfs.fasta contigs_new_nohit_orfs_sort.fasta

mv contigs_new_nohit.fasta novel_contigs.fasta
mv contigs_new_nohit_orfs_sort.fasta novel_orfs.fasta

#rm -f $1_diam_ref.txt
rm -f $1_diam_ref_hits.txt
#rm -f $1_diam_ref_hits_sort.txt
rm -f $1_diam_ref_hits_sort_cov.txt

#rm -f $1_diam_nr.txt

rm -f contigs_new.fasta
rm -f contigs_new_hit.fasta
rm -f contigs_new_nohit_orfs.fasta

cd ../
