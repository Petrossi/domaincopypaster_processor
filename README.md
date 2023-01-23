Instruction 

cd docker
sh restart.sh

TODO 
    1.State object in store service - done 
    2.Stone in the middle for workers  
    3.UrlProcessor.getFilters refactor optimization - done
    4.domain crawling info refresh counters on start - done
    5.Event listener on page crawled (add to import list, log to state object)
    6.Integrate reactive    
    7.Refactor Filter from db into FilterParserService
    
Research
    https://www.screamingfrog.co.uk/seo-spider/user-guide/tabs/#uri
    https://sitebulb.com/hints/indexability/head-contains-a-noscript-tag-which-includes-an-image/
    
Working
app start - sh app.sh start
app log - sh app.sh log
app stop - sh app.sh stop

Server deploy: 
sh tourzy_start.sh build
Server start: 
ssh root@157.230.91.80 "java -Dspring.profiles.active=tourzy -jar /var/www/domainsuervey/crawler/domainsurvey.jar > /var/www/domainsuervey/crawler/init.log 2>/var/www/domainsuervey/crawler/error.log & echo"
Server log: 

ssh root@157.230.91.80

ssh root@157.230.91.80 "tail -f /var/www/domainsuervey/crawler/init.log"
ssh root@157.230.91.80 "kill -9 $(ps ax | grep 'domainsurvey' | fgrep -v grep | awk '{ print $1 }')"

start
nohup java -Dspring.profiles.active=tourzy -jar /var/www/domainsuervey/crawler/domainsurvey.jar > /var/www/domainsuervey/crawler/init.log 2>/var/www/domainsuervey/crawler/error.log & echo

kill
kill -9 $(ps ax | grep 'domainsurvey' | fgrep -v grep | awk '{ print $1 }')

log
tail -f /var/www/domainsuervey/crawler/init.log