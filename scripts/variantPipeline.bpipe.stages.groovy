// fastqc generates two zipped directory files using a custom naming convention
// input: two *.fastq files
// output: two *_fastqc.zip files
fastqc = {
    doc "Run FASTQC to generate QC metrics for the fastq files"
    output.dir = "fastqc"
    output_dir = "fastqc"
    if(input.input.size == 2){
    	produce("${output_dir}/*_fastqc.zip") {
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input1"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input2"
    		}
	}
    if(input.input.size == 4){
    	produce("${output_dir}/*_fastqc.zip") {
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input1"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input2"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input3"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input4"
    		}
	}
    if(input.input.size == 6){
    	produce("${output_dir}/*_fastqc.zip") {
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input1"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input2"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input3"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input4"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input5"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input6"
    		}
	}
   if(input.input.size == 8){
    	produce("${output_dir}/*_fastqc.zip") {
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input1"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input2"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input3"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input4"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input5"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input6"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input7"
        	exec "fastqc -o ${output_dir} --noextract -f fastq $input8"
    		}
	}
	forward input1,input2
}

samtools_mapq_filter ={
	doc " remove the reads that fail a mapping quality cut off"
	output.dir = "filter"
	filter("filtered"){
		exec "samtools view -Shq 30 $input > $output"
	}
}

// Cutadapt will remove adapters trim to end bases below Q 25 and 
// remove any remaining reads that are less than 20 bases long.
// NEBNEXT ADAPTOR AGATCGGAAGAGCACACGTCTGAACTCCAGTC 
cutadapt = {
	doc " Cutadapt will remove adapters trim to end bases below Q 25 and  remove any remaining reads that are less than 20 bases long"
	output.dir = "cutadapt"
	def out_1 = './cutadapt/'+file(input1).name.split("\\.[12]\\.[0-9]\\.fastq")[0]+ '.1.1.trimmed.fastq'
	def out_2 = './cutadapt/'+file(input2).name.split("\\.[12]\\.[0-9]\\.fastq")[0]+ '.2.1.trimmed.fastq'
	def log_file = './cutadapt/'+file(input1).name.split("\\.[12]\\.[0-9]\\.fastq")[0]+ '.log'
	filter("trimmed"){
		exec """
		cutadapt -a AGATCGGAAGAGCACACGTCTGAACTCCAGTC -A AGATCGGAAGAGCACACGTCTGAACTCCAGTC -q 25 -m 20 
    		-o ${out_1} -p ${out_2} 
    		$input1.fastq $input2.fastq 
    		> ${log_file}
		"""
	}
	forward out_1, out_2

}

// pydmx generates a directory of demultiplexed fastqs named using the specified barcode file
//     it requires python2.7
// input: two *.fastq files and a bars.csv file
// intermediate output:
//        trimmed: two fastq files derived from original fastq pairs trimmed to shortest sequence in the pair
//        multiplexed: two fastqs with control sequences removed and duplication consolidated along with a summary text file
//   final output:
//        demultiplexed: a pair of fastq files for each barcoded sample (defined by specific barcode file)
pydmx = {
    doc "Runs pydmx to reformat the fastq header and generate consensus sequence"
    out_dir = "02-pydmx/demultiplexed"
    output.dir = "02-pydmx/demultiplexed"
    produce("${out_dir}/*.fastq") {
        exec "python2.7 ${PYDMX} -l $input1.fastq -r $input2.fastq -b ${BARS} -o 02-pydmx "
    }
}


// input: 'r1' and 'r2' fastq files
// output: a *.sam file
//bowtie2_M= {
//    doc "Aligns using Bowtie, generating a SAM file.  Note, this file may be very large."
//    output.dir = "align"
//    produce ("align/*.sam") {
//        exec "bowtie2 --seed 42 --sensitive -x ${REFERENCE} -1 $input1 -2 $input2 -S ./align/" + new File(input1).name.split("\\.[12]\\.fastq")[0] + 'sam' 2> new File(input1).name.split("\.[12]\.[0-9]\.fastq")[0] + '.log'
//    }
//}
bowtie2 = {
    doc "Aligns using Bowtie, generating a SAM file.  Note, this file may be very large."
    output.dir = "align"
    if(input.input.size == 2){
 	def sam_out='./align/'+file(input1).name.split("\\.[12]\\.[0-9]\\.trimmed.fastq")[0]+ '.sam'
	def log_file = './align/'+file(input1).name.split("\\.[12]\\.[0-9]\\.trimmed.fastq")[0]+ '.log'
	//println "expected outout " + sam_out
	produce(sam_out) {
            exec "bowtie2 --seed 42 --sensitive-local -x ${REFERENCE} -1 $input1 -2 $input2 -S ${sam_out} 2> ${log_file}"
        }
    }
    if(input.input.size == 4){
        def sam_out='./align/'+file(input1).name.split("\\.[12]\\.[0-9]\\.fastq")[0]+ '.sam'
        def log_file = './align/'+file(input1).name.split("\\.[12]\\.[0-9]\\.fastq")[0]+ '.log'
	produce(sam_out) {
            exec "bowtie2 --seed 42 --sensitive-local -x ${REFERENCE} -1 $input1,$input2 -2 $input3,$input4 -S ${sam_out} 2> ${log_file}"
        }
    }
    if(input.input.size == 6){
 	def sam_out='./align/'+file(input1).name.split("\\.[12]\\.[0-9]\\.fastq")[0]+ '.sam'
	def log_file = './align/'+file(input1).name.split("\\.[12]\\.[0-9]\\.fastq")[0]+ '.log'
	produce(sam_out) {
            exec "bowtie2 --seed 42 --sensitive-local -x ${REFERENCE} -1 $input1,$input2,$input3 -2 $input4,$input5,$input6 -S ${sam_out} 2> ${log_file}"
        }
    }
    if(input.input.size == 8){
 	def sam_out='./align/'+file(input1).name.split("\\.[12]\\.[0-9]\\.fastq")[0]+ '.sam'
	def log_file = './align/'+file(input1).name.split("\\.[12]\\.[0-9]\\.fastq")[0]+ '.log'
	produce(sam_out) {
            exec "bowtie2 --seed 42 --sensitive-local -x ${REFERENCE} -1 $input1,$input2,$input3,$input4 -2 $input5,$input6,$input7,$input8 -S 2> ${log_file}"
        }
    }
}

picard_sortsam = {
    doc "Sort SAM file so that its in reference order and convert to BAM."
    tmp_dir    = "./tmp"
    output.dir = "align"
    transform("bam") {
        exec """
            java -Xmx4g -Djava.io.tmpdir=$tmp_dir -jar ${LIBRARY_LOCATION}/picard-tools-1.133/picard.jar SortSam
            SO=coordinate
            INPUT=$input.sam
            OUTPUT=$output
            VALIDATION_STRINGENCY=LENIENT
            CREATE_INDEX=true
        """
    }
}


picard_removedups = {
    doc "Remove duplicates"
    tmp_dir    = "./tmp"
    output.dir = "removed_duplicates"
    filter("removed") {
        exec """
            java -Xmx2g -Djava.io.tmpdir=$tmp_dir -jar ${LIBRARY_LOCATION}/picard-tools-1.133/picard.jar MarkDuplicates
            INPUT=$input.bam
            OUTPUT=$output
            REMOVE_DUPLICATES=true
            CREATE_INDEX=true
            METRICS_FILE=${output}-picard.out.metrics
            VALIDATION_STRINGENCY=LENIENT
        """
    }
}

picard_markdups = {
    doc "Mark  duplicates"
    tmp_dir    = "./tmp"
    output.dir = "marked_duplicates"
    filter("marked") {
        exec """
            java -Xmx1g -Djava.io.tmpdir=$tmp_dir -jar ${LIBRARY_LOCATION}/picard-tools-1.115/MarkDuplicates.jar
            INPUT=$input.bam
            OUTPUT=$output
            REMOVE_DUPLICATES=false
            CREATE_INDEX=true
            METRICS_FILE=${output}-picard.out.metrics
            VALIDATION_STRINGENCY=LENIENT
        """
    }
}



deepsnv = {
	doc "Runs a basic deepSNV script to call variants in each sample"
	output.dir = "deepSNV"
	def in_bam=file(input).name
	def control = file(CONTROL_BAM).name.replace(".bam","")
	def test = file(input.bam).name.replace(".bam","")
	println "test:" + test
	println "control:" + control
				transform("csv","fasta"){
					exec "Rscript  --vanilla --slave ${SCRIPTS}/deepSNV.R ${REFERENCE_FA} $input1 $CONTROL_BAM bonferroni ${P_CUT} ${P_COM_METH} ${DISP} ${STRINGENT_FREQ} $output.csv $output.fasta ${R_LIB}"
				}

}

mapq_conditional = {
        doc "runs a python script that for each variant called calculates the average mapped quality of the variant and adds the whole summary line + this value to the summary.csv file "
        output.dir="Variants"
        //I've been having some trouble getting the right inputs and outputs to line up. The best way to do this might be to make a map that explicitly describes how the sample pair up. bUt that might take a little too long to figure out how to do now.
        def csv = file(input.csv).name.replace(".csv","")
        def bam = file(input.bam).name.replace(".bam","")
        def control = file(CONTROL_BAM).name.replace(".bam","")
	if (csv!=control){
		if ( csv == bam) {
                	println "Found match: " + csv + ".csv and " +bam+".bam"
                	transform("mapq.sum.csv","mapq.reads.csv"){
                        	exec " python ${SCRIPTS}/mapq.py $input.csv $input.bam $output1.csv $output2.csv"
        			}
        	} else {
                	println "csv: " + csv + " doesn't match bam: "+bam
        	}
          }
}


reference_base_and_quality = {
        doc "runs a python script that for each variant called checks the number of reference bases (skipped by deepSNV) and then calculates the average mapped quality, phred and read position of all variants"
        output.dir="Variants"
        //I've been having some trouble getting the right inputs and outputs to line up. The best way to do this might be to make a map that explicitly describes how the sample pair up. bUt that might take a little too long to figure out how to do now.
        def csv = file(input.csv).name.replace(".csv","")
        def bam = file(input.bam).name.replace(".bam","")
        def control = file(CONTROL_BAM).name.replace(".bam","")
	if (csv!=control){
		if ( csv == bam) {
                	println "Found match: " + csv + ".csv and " +bam+".bam"
                	transform("ref.sum.csv"){
                        	exec " python ${SCRIPTS}/reciprocal_variants.py $input.bam $input.csv $output.csv ${OPTIONS}"
        			}
        	} else {
                	println "csv: " + csv + " doesn't match bam: "+bam
        	}
          }
}


parse= {
	doc "Take the concatenated consensus fasta file from deepSNV and deconcatenate it using the segmented positions in from the coverage file"
	output.dir = "parsed_fa"
	filter("parsed"){
		exec "python ${SCRIPTS}/parse_consensus.py ${REFERENCE_FA} $input $output "
	}
	forward input.csv
}



classification = { 
        doc "Add amino acid data to variant calls based a reference sequence"
        output.dir = "Final_variants"
        def ref = "./parsed_fa/"+file(CONTROL_BAM).name.replace(".bam",".parsed.fasta")    
	filter("AA"){
                exec "python ${SCRIPTS}/AA_var.py  $ref $input.csv $output ${OPTIONS}"
                }   
}

sift = {
	doc "Variant csv file and filter based on quality scores"
	output.dir = "Filter_var"
	filter("filtered"){
		exec "python ${SCRIPTS}/filter_var.py $input $output ${OPTIONS} "
	}
}



combine = {
	doc "combines the coverage, reads and variant calls into one file that can easily be imported into R for analysis"

	exec "python ${SCRIPTS}/combine.py ./Final_variants AA.csv ../all.variants.csv"
	exec "python ${SCRIPTS}/combine.py ./deepSNV cov.csv ../all.coverage.csv"
}

quality_report = {
	doc "runs MultiQC to create a quality report for the run"
	exec "multiqc ./"
}

consensus = {
	doc "finds the consensus of each sample read depth cutoff at default 1000"
	output.dir = "consensus"
	transform("fasta"){
		exec " python ${SCRIPTS}/consensus.py ${BEDJSON} $input.bam $output.fasta --all "
		}
	forward input.bam
	}
position_stats = {
    doc "gets position stats required refernce file in consensus directory named as sample.removed.fasta"
    output.dir = "position-stats"
    def out = "./position-stats/"+file(input.bam).name.replace(".bam","")
	def reference_fa = "./consensus/"+file(input.bam).name.replace(".bam",".fasta")
        transform("json"){
        exec "python ${SCRIPTS}/position_data.py  ${BEDJSON} ${reference_fa} $input.bam  $output.json --maxDepth ${MAXDEPTH} "
    }
}
parseJson = {
	doc "Parse the output json to a csv for working nicely in R"
	output.dir = "position-stats-csv"
	transform("csv"){
		exec "python ${SCRIPTS}/variantJSONtocsv.py $input $output.csv"
	}
}