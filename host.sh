echo "HOST MAP"
#Map the reads to the host Vampire Bat genome, and then the PhiX genome, extract the unmapped reads to $1_unmap1.fastq and $1_unmap2.fastq

for fastq in *.fq
do
	if [[ $fastq == *_val_1.fq* ]]
		then
		R1=$fastq
		echo "Trim Read1 file = $R1"
	fi

        if [[ $fastq == *_val_2.fq* ]]
                then
                R2=$fastq
                echo "Trim Read2 file = $R2"
        fi
done

bowtie2 -p 10 --local -x ~berg01l/ngs/BatGenomes/vampire -1 $R1 -2 $R2 -S $1.sam > $1_bt2_vampire.txt 2>&1

samtools view -@ 10 -bS $1.sam > $1.bam
samtools sort -@ 10 $1.bam -o $1.bam
samtools index $1.bam

bam2fastq -f --unaligned --no-aligned -o $1_unmap#.fastq $1.bam

echo "vampire reads" >> $1_log.txt
expr `(wc -l $1_unmap_1.fastq |cut -f1 -d " ")` / 4 >> $1_log.txt

bowtie2 -p 10 --local -x ~berg01l/ngs/BatGenomes/phix/genome -1 $1_unmap_1.fastq -2 $1_unmap_2.fastq -S $1_phix.sam > $1_bt2_phix.txt 2>&1

samtools view -@ 10 -bS $1_phix.sam > $1_phix.bam
samtools sort -@ 10 $1_phix.bam -o $1_phix.bam
samtools index $1_phix.bam

bam2fastq -f --unaligned --no-aligned -o $1_unmap_phix#.fastq $1_phix.bam

echo "phix reads" >> $1_log.txt
expr `(wc -l $1_unmap_phix_1.fastq |cut -f1 -d " ")` / 4 >> $1_log.txt

mv $1_unmap_phix_1.fastq $1_unmap_1.fastq
mv $1_unmap_phix_2.fastq $1_unmap_2.fastq

rm -f $1.sam
rm -f $1.bam
rm -f $1.bam.bai
rm -f $1_phix.sam
rm -f $1_phix.bam
rm -f $1_phix.bam.bai

