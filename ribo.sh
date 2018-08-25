echo "RIBO PICK"

#Interleave the two fastq files into 1 file - as ribo picker does not do paired end
java -jar ~orto01r/dist/ortools.jar -i $1_unmap_1.fastq $1_unmap_2.fastq $1_unmap

ribopicker.pl -t 10 -id $1_ribo -f $1_unmap.fastq -dbs ssr123,slr123 -keep_tmp_files

#De-interleave - removes unmatched pairs
java -jar ~orto01r/dist/ortools.jar -d $1_ribo_nonrrna.fq

mv $1_ribo_nonrrna_1.fastq $1_nonrrna_1.fastq
mv $1_ribo_nonrrna_2.fastq $1_nonrrna_2.fastq

echo "ribopicker reads" >> $1_log.txt
expr `(wc -l $1_nonrrna_1.fastq |cut -f1 -d " ")` / 4 >> $1_log.txt

rm -r $1_unmap.fastq
rm -r $1_ribo_nonrrna.fq
rm -f $1_ribo_rrna.fq
rm -f $1_unmap_1.fastq
rm -f $1_unmap_2.fastq

