echo "TRIM"

touch $1_log.txt

for fastq in *.fastq
do
	if [[ $fastq == *_R1_001.fastq* ]]

		then
		R1=$fastq
		echo "Read1 file = $R1"
	fi

        if [[ $fastq == *_R2_001.fastq* ]]
                then
                R2=$fastq
                echo "Read2 file = $R2"
        fi
done

echo "Raw reads" >> $1_log.txt
expr `(wc -l $R1 |cut -f1 -d " ")` / 4 >> $1_log.txt

trim_galore -q 25 --dont_gzip --length 75 --clip_R1 14 --paired $R1 $R2 > trim_galore_out.txt 2>&1 

#gzip $R1
#gzip $R2

for fastq in *.fq
do
	if [[ $fastq == *_val_1.fq* ]]
		then
		T1=$fastq
		echo "Trim Read1 file = $T1"
	fi

        if [[ $fastq == *_val_2.fq* ]]
                then
                T2=$fastq
                echo "Trim Read2 file = $T2"
        fi
done

echo "trim_galore reads" >> $1_log.txt
expr `(wc -l $T1 |cut -f1 -d " ")` / 4 >> $1_log.txt

prinseq-lite.pl -lc_method dust -lc_threshold 7 -derep 12345 -fastq $T1 -fastq2 $T2 -out_good prinseq_good -out_bad prinseq_bad > prinseq_out.txt 2>&1

echo "prinseq reads" >> $1_log.txt
expr `(wc -l prinseq_good_1.fastq |cut -f1 -d " ")` / 4 >> $1_log.txt

mv prinseq_good_1.fastq $T1
mv prinseq_good_2.fastq $T2

rm -f prinseq_good_1_singletons.fastq
rm -f prinseq_good_2_singletons.fastq
rm -f prinseq_bad_1.fastq
rm -f prinseq_bad_2.fastq
