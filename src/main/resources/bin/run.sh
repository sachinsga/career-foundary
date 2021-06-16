#!/bin/bash
script_dir=($(dirname "$0"))
parent_dir=($(dirname -- "$script_dir"))

echo "Executing Command: spark-submit --deploy-mode cluster --class StandardDeviation --files $parent_dir/etc/challenge.conf $parent_dir/lib/Challenge-1.0-SNAPSHOT.jar challenge.conf"

spark-submit --deploy-mode cluster --class StandardDeviation --files $parent_dir/etc/challenge.conf $parent_dir/lib/Challenge-1.0-SNAPSHOT.jar challenge.conf