create view V_ALF_TRANSACTION
AS
SELECT ID, VERSION, SERVER_ID, CHANGE_TXN_ID,
to_timestamp(
      to_char(
		to_date('01-01-1970 00:00:00', 'DD-MM-YYYY HH24:MI:SS')
			+ NUMTODSINTERVAL(FLOOR(COMMIT_TIME_MS / (1000)),'SECOND')
			,'dd-mm-yyyy hh24:mi:ss')
			||
       to_char (  (COMMIT_TIME_MS / (1000)) - FLOOR(COMMIT_TIME_MS / (1000)) ,'.999999')    ,'dd-mm-yyyy hh24:mi:ss.FF'
   )commit_timestamp
FROM ALF_TRANSACTION