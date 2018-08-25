# Allmond
Allmond is a simple bioinformatics pipeline for viral metagenomics, it centres around the tool DIAMOND (https://github.com/bbuchfink/diamond) for blastx-ing reads and contigs for classification. Allmond consists of a number of simple sequential bash scripts to filter paired end reads (typically from Illumina) followed by de novo assembly.

It uses trim_galore (https://www.bioinformatics.babraham.ac.uk/projects/trim_galore/) for illumina adapter and quality trimming, prinseq (http://prinseq.sourceforge.net) to remove low complexity and duplicate reads, mapping to host genomes (in this case vampire bat and PhiX control) with bowtie2 (http://bowtie-bio.sourceforge.net/bowtie2/index.shtml)  followed extraction of unmapped reads with bam2fastq (https://github.com/jts/bam2fastq), DIAMOND blastx of reads followed by removal of bacterial and eukaryotic reads, de novo assembly of reads using SPAdes (http://cab.spbu.ru/software/spades/), followed by DIAMOND blastx of the final contigs. We used this pipeline to analyse the virome of vampire bat samples in Peru and to identify potential novel viral contigs of intrest.

The scripts are currently hard coded to use the DIAMOND databases and host genomes on our server (and their respective paths). We are sharing these scripts as is, but are actively developing a more modular approach which can easily be configured to other servers/databases/genomes. We are also developing a heatmap based visualisation tool for the end of the pipeline.

Within a folder with 2 paired end FASTQ files type to run (where outputName is the stem/stub to use for output files):

./run.sh outputName
