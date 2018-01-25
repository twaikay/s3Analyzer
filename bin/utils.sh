#!/bin/ksh

binDir=${0%/*}
homeDir=$binDir/..
envDir=$homeDir/env
logDir=$homeDir/log
tmpDir=$homeDir/temp

cd $homeDir

################################################################

appOp=$1
appParam1=$2
appParam2=$3
appParam3=$4

################################################################

runAll()
{
srcDir=$appParam1

#srcDir=~/aws/lambda
functionName=handleRequest

for ffile in `ls $srcDir/*.js`; do
#echo ffile is $ffile haha
	ffullname=${ffile##*/}
	fname=$(echo $ffullname | cut -f 1 -d '.')
#echo fname is $fname haha	
#echo "aws lambda invoke --function-name handleRequest $srcDir/${fname}.log --payload file://${ffile}"
echo "spawning aws lambda process to $srcDir/${fname}.log ..."
	aws lambda invoke --function-name handleRequest $srcDir/${fname}.log --payload file://${ffile} &
done
echo "done"
}

################################################################

genFiles()
{
#buckets=$appParam1
noOfDirs=$appParam1
alphaPrefix=$appParam2
dDir=$appParam3

destDir=$homeDir/test/buckets/$dDir
rm -rf ${destDir}
mkdir -p ${destDir}

alphas="a b c d e f g h i j k l m n o p q r s t u v q x y z"
set -A alphasArr $alphas
let alphasArrSize=${#alphasArr[*]}
#set -A bucketsArr $buckets
#let bucketsArrSize=${#bucketsArr[*]}
#totalFilesPerBucket=$(($alphasArrSize * $noOfDirs))
#echo "creating ${totalFilesPerBucket} files in each bucket, total: $bucketsArrSize buckets..."

let totalFiles=noOfDirs*alphasArrSize*alphaPrefix
echo "creating ${totalFiles} files..."
#return

#for bucketName in "${bucketsArr[@]}"; do
	let numVal=1
	while [ $numVal -le $noOfDirs ]; do
		for alphaVal in "${alphasArr[@]}"; do
			randNo=$RANDOM
#echo randNo is $randNo haha
			dDir=${destDir}/${bucketName}/${numVal}
			mkdir -p $dDir
			let alphaPrefixCounter=1
			while [ $alphaPrefixCounter -le $alphaPrefix ]; do
				ffile=${dDir}/${alphaVal}${alphaPrefixCounter}_${randNo}.txt
				echo "hello man ${randNo}" > $ffile
				let alphaPrefixCounter=alphaPrefixCounter+1
			done
		done
		let numVal=numVal+1
	done
#echo "${bucketName}: created $totalFilesPerBucket files..."
echo "created $totalFilesPerBucket files..."
#done
}

################################################################

eval "${appOp}"
