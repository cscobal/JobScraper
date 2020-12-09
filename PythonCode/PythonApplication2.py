import MySQLdb



def remove_html(data):
    size = len(data)
    start_index = 0
    end_index = 0
    removing = 0
    newData = data
    while(end_index < size and start_index < size):
        if(removing == 0):
            if(data[start_index] == '<'):
                removing = 1
                end_index += 1
            elif(data[start_index] == '>'):
                print("FAILURE")
                print(data)
                break
            else:
                start_index += 1
        else:
            if(data[end_index] == '>'):
                removing = 0
                newData = newData.replace(data[start_index:end_index + 1], '')
                end_index += 1
                start_index = end_index
            else:
                end_index += 1
    if(removing == 1):
        print("STILL REMOVING???")
        print(data)
        return ''
    else:
        if(len(newData) > 0):
            if(newData[0] == ' '):
                if(len(newData) > 1):
                    if(newData[1] == ' '):
                        if(len(newData) > 2):
                            if(newData[2] == ' '):
                                return newData[3:len(newData)]
                        return newData[2:len(newData)]
                return newData[1:len(newData)]
            else:
                return newData
        else:
            return ""




db = MySQLdb.connect(host="localhost",    # your host, usually localhost
                     user="root",         # your username
                     passwd="",  # your password
                     db="job_database")

cur = db.cursor()

cur.execute("SELECT job_id, job_text FROM jobs where job_processed = 2 AND job_apply = 0")

# print all the first cell of all the rows

rows = cur.fetchall()
print(len(rows))
for row in rows:
    jobID = row[0]
    jobText = ""
    jobTextList = []
    if(not row[1] == None):
        jobText = unicode(row[1].lower().replace(",", " ").replace('.', '').replace('?', '').replace('•', ''), errors='ignore')
        jobTextList = jobText.split('\n')
    jobTextSentences = []
    cont = 0
    for s in jobTextList:
        data = None
        if(len(s) > 0):
            data = remove_html(s)    
        if(data != None):
            sentenceWords = data
            if(len(sentenceWords) > 0):
                if((sentenceWords[0] == 'preferred' and (sentenceWords[1] == 'qualifications' or sentenceWords[1] == 'requirements'))
                   or (sentenceWords[0] == 'nice' and sentenceWords[1] == 'to' and sentenceWords[2] == 'have')
                   or (sentenceWords[0] == 'nice-to-have')
                   or (sentenceWords[0] == 'pluses')
                   or (sentenceWords[0] == 'bonus' and sentenceWords[1] == 'points')):
                    cont = 1
            if(cont == 0):
                jobTextSentences.append(data)
    qualified = 0
    reason_qualified = ""
    if(row[1] == None):
        qualified = -1
        reason_qualified = "expired"
    else:
        for s in jobTextSentences:
            sentenceWords = s.split()     
            if(((any(char.isdigit() for char in s) and ("professional" in sentenceWords or "industry" in sentenceWords or "commercial" in sentenceWords or "as a software developer" in s or "software development experience" in s or "software engineering experience" in s) and ("year" in sentenceWords or "years" in sentenceWords)) or 'currently pursuing' in s)):
                if(not("0" in s)):
                    qualified = 3
                    reason_qualified = s
                else:
                    if(qualified < 2):
                        qualified = 2
                        reason_qualified = s
            elif(any(char.isdigit() for char in s) and "experience" in sentenceWords):
                if(qualified < 1):
                    qualified = 1
                    reason_qualified = s

    if(qualified == 0):
        cur.execute("UPDATE jobs SET job_processed = 3, job_apply = 0 WHERE job_id = '" + (str)(jobID) + "'")
    else:
        cur.execute("UPDATE jobs SET job_processed = 3, is_qualified = '" + (str)(qualified) + "', reason_qualified = '" + (str)(reason_qualified) + "' WHERE job_id = '" + (str)(jobID) + "'")
    

cur.execute("SELECT job_id, job_title, job_url, job_apply, job_date FROM jobs WHERE job_processed = 3 AND is_qualified = 0")

rows = cur.fetchall()

for row in rows:
    cur.execute("INSERT INTO qualified_jobs (job_id, job_title, job_url, job_apply, job_date) VALUES ('" + (str)(row[0]) + "', '" + (str)(row[1]) + "', '" + (str)(row[2]) + "', '" + (str)(row[3]) + "', '" + (str)(row[4]) + "')")

cur.execute("SELECT job_id, job_title, job_url, job_apply, job_processed, job_date, reason_qualified FROM jobs WHERE job_processed = 3 AND is_qualified = 1")

rows = cur.fetchall()

for row in rows:
    cur.execute("INSERT INTO possible_jobs (job_id, job_title, job_url, job_apply, job_processed, job_date, reason_qualified) VALUES ('" + (str)(row[0]) + "', '" + (str)(row[1]) + "', '" + (str)(row[2]) + "', '" + (str)(row[3]) + "', 0, '" + (str)(row[5]) + "', '" + (str)(row[6]) + "')")

db.commit()
db.close()
