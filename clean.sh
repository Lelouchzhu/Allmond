echo "DIAMOND ALL REFSEQ"

#Interleave the reads - as dimaond does not handle paired end reads
java -jar ~orto01r/dist/ortools.jar -i $1_nonrrna_1.fastq $1_nonrrna_2.fastq $1_nonrrna
 
mkdir -p diam_temp

diamond blastx -d /home2/db/diamond/refseq_protein -f 6 -o $1_diam.txt -p 10 -q $1_nonrrna.fastq -t diam_temp 
ktImportBLAST $1_diam.txt -o $1_diam.html > $1_kt.txt

echo "ALLMOND-FILTER"
java -jar ~orto01r/dist/AllmondGetTaxonSeqs.jar $1_diam.html 10239
#java -jar ~orto01r/dist/AllmondGetTaxonSeqs.jar $1_diam.html 0
#cat $1_diam_10239_names.txt $1_diam_0_names.txt > $1_diam_names.txt
mv $1_diam_10239_names.txt $1_diam_names.txt

#Pull out all the viral and unassigned reads - as specified in the _names.txt file
java -jar ~orto01r/dist/AllmondFilterSequencesByName.jar -s1 $1_nonrrna.fastq -id1 $1_diam_names.txt -e 0

#Pull out reads with no dimaond hits - $1_nonrrna_nohit.fastq and $1_nonrrna_hit.fastq created
java -jar ~orto01r/dist/AllmondHits.jar -i $1_diam.txt -r $1_nonrrna.fastq

#Combine the different reads together
cat $1_nonrrna_fil.fastq $1_nonrrna_nohit.fastq > $1_clean.fastq

#One thing to check - if we have one read of a pair matched to a virus and the other not - is it still pulled out or not (depends on if hit to something else or nothing)

#Deinterleave - removes mismatched pairs
java -jar ~orto01r/dist/ortools.jar -d $1_clean.fastq

echo "viral reads - singletons not pairs" >> $1_log.txt
expr `(wc -l $1_nonrrna_fil.fastq |cut -f1 -d " ")` / 4 >> $1_log.txt

echo "dark reads - singletons not pairs" >> $1_log.txt
expr `(wc -l $1_nonrrna_nohit.fastq |cut -f1 -d " ")` / 4 >> $1_log.txt

echo "cleaned reads" >> $1_log.txt
expr `(wc -l $1_clean_1.fastq |cut -f1 -d " ")` / 4 >> $1_log.txt

rm -f $1_clean.fastq

rm -f $1_diam_names.txt
rm -f $1_diam_10239_names.txt
rm -f $1_diam_0_names.txt
rm -f $1_diam_hits.txt
#rm -f $1_diam.txt

rm -f $1_nonrrna_1.fastq
rm -f $1_nonrrna_2.fastq
rm -f $1_nonrrna.fastq
rm -f $1_nonrrna_fil.fastq
rm -f $1_nonrrna_hit.fastq
rm -f $1_nonrrna_nohit.fastq

#Results in $1_clean_1.fastq and $1_clean-2.fastq

