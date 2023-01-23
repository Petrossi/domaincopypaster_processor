Deploy

sh prod_app.sh build

start nohup java -Dspring.profiles.active=alphacrawler -jar /var/www/crawler/domainsurvey.jar >
/var/www/crawler/init.log 2>/var/www/crawler/error.log & echo

kill kill -9 $(ps ax | grep 'domainsurvey' | fgrep -v grep | awk '{ print $1 }')

log tail -f /var/www/crawler/init.log