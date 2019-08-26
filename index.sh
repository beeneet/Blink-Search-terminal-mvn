# SCRIPTPATH=$( cd $(dirname $0) ; pwd -P )
java -Xmx400M -cp target/classes com.blink.search.Index Basic $1 $2
