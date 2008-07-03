#include <string.h>
#include <mysql.h>

my_bool raise_error_init(
    UDF_INIT *initid
,   UDF_ARGS *args
,   char *message
){ 
    unsigned int argStringLength;
    if(args->arg_count==1
    && args->arg_type[0]==STRING_RESULT)
    {
        argStringLength = strlen(args->args[0]) 
                        + 1
                        ;
        memcpy(
            message
        ,   args->args[0]
        ,   argStringLength>MYSQL_ERRMSG_SIZE
        ?   MYSQL_ERRMSG_SIZE
        :   argStringLength
        );
    } else {
        memcpy(
            message
        ,   "Unspecified error raised"
        ,   25
        );
    }    
    return 1;
}

long long raise_error(
    UDF_INIT *initid
,   UDF_ARGS *args
,   char *is_null
,   char *error
){
    return 0;
}
