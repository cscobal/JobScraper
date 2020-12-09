import MySQLdb

db = MySQLdb.connect(host="localhost",    # your host, usually localhost
                     user="root",         # your username
                     passwd="Lasagna_9",  # your password
                     db="job_database")

cur = db.cursor()

cur.execute("SELECT job_id, job_title FROM jobs where job_processed != -1 AND job_apply = 0")

wrong_words = ["teacher", "executive", "ceo", "electrician", "accountant", "electrical", "cfo", "director", "mgr", "mgr.", "lead", "experienced", "vp", "manager",
               "sr", "sr.", "5", "4", "3", "v", "iv", "iii", "master", "masters", "master's", "staff", "senior", "phd", "administrator", "recruiter", "consultant", "artist", "writer", "paralegal"]

probable_words = ["sde", "engineer", "developer", "development", "azure", "devops", "programmer", ".net", "android", "it", "qa", "sdet", "java", "javascript", "technical", "technician"]

probable_set = set(probable_words)

wrong_set = set(wrong_words)
# print all the first cell of all the rows
print("??")
for row in cur.fetchall():
    qualified = 1
    title = unicode(row[1].replace("-","").replace(",","").replace("/", "").replace("(", "").replace(")","").lower(), errors='ignore')
    title_words = title.split(" ")
    title_set = set(title_words)
    if(title_set & probable_set):
        qualified = 2
    if(title_set & wrong_set):
        qualified = 0
    if(qualified == 1):
        cur.execute("UPDATE jobs SET job_processed = 1 WHERE job_id = '" + (str)(row[0]) + "'")
    elif(qualified == 2):
        cur.execute("UPDATE jobs SET job_processed = 2 WHERE job_id = '" + (str)(row[0]) + "'")
    else:
        cur.execute("UPDATE jobs SET job_processed = -1 WHERE job_id = '" + (str)(row[0]) + "'")

db.commit()
db.close()