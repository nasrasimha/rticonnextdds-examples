/*******************************************************************************
 (c) 2005-2014 Copyright, Real-Time Innovations, Inc.  All rights reserved.
 RTI grants Licensee a license to use, modify, compile, and create derivative
 works of the Software.  Licensee has the right to distribute object form only
 for use with RTI products.  The Software is provided "as is", with no warranty
 of any type, including any warranty for fitness for any purpose. RTI is under
 no obligation to maintain or support the Software.  RTI shall not be liable for
 any incidental or consequential damages arising out of the use or inability to
 use the software.
 ******************************************************************************/
/* ccf_publisher.c

 A publication of data of type ccf

 This file is derived from code automatically generated by the rtiddsgen
 command:

 rtiddsgen -language C -example <arch> ccf.idl

 Example publication of type ccf automatically generated by 'rtiddsgen'. To test
 them follow these steps:

 (1) Compile this file and the example subscription.

 (2) Start the subscription with the command
 objs/<arch>/ccf_subscriber <domain_id> <sample_count>

 (3) Start the publication with the command
 objs/<arch>/ccf_publisher <domain_id> <sample_count>

 (4) [Optional] Specify the list of discovery initial peers and  multicast
 receive addresses via an environment variable or a file (in the current working
 directory) called NDDS_DISCOVERY_PEERS.

 You can run any number of publishers and subscribers programs, and can add and
 remove them dynamically from the domain.


 Example:

 To run the example application on domain <domain_id>:

 On Unix:

 objs/<arch>/ccf_publisher <domain_id>
 objs/<arch>/ccf_subscriber <domain_id>

 On Windows:

 objs\<arch>\ccf_publisher <domain_id>
 objs\<arch>\ccf_subscriber <domain_id>


 modification history
 ------------ -------
 20May2014,amb Example adapted for RTI Connext DDS 5.1
 */

#include <stdio.h>
#include <stdlib.h>
#include "ndds/ndds_c.h"
#include "ccf.h"
#include "ccfSupport.h"
/* Custom filter defined here */
#include "filter.c"

/* Delete all entities */
static int publisher_shutdown(DDS_DomainParticipant *participant) {
    DDS_ReturnCode_t retcode;
    int status = 0;

    if (participant != NULL) {
        retcode = DDS_DomainParticipant_delete_contained_entities(participant);
        if (retcode != DDS_RETCODE_OK) {
            printf("delete_contained_entities error %d\n", retcode);
            status = -1;
        }

        retcode = DDS_DomainParticipantFactory_delete_participant(
                DDS_TheParticipantFactory, participant);
        if (retcode != DDS_RETCODE_OK) {
            printf("delete_participant error %d\n", retcode);
            status = -1;
        }
    }

    /* RTI Connext provides finalize_instance() method on
       domain participant factory for people who want to release memory used
       by the participant factory. Uncomment the following block of code for
       clean destruction of the singleton. */
/*
    retcode = DDS_DomainParticipantFactory_finalize_instance();
    if (retcode != DDS_RETCODE_OK) {
        printf("finalize_instance error %d\n", retcode);
        status = -1;
    }
*/

    return status;
}

static int publisher_main(int domainId, int sample_count) {
    DDS_DomainParticipant *participant = NULL;
    DDS_Publisher *publisher = NULL;
    DDS_Topic *topic = NULL;
    DDS_DataWriter *writer = NULL;
    ccfDataWriter *ccf_writer = NULL;
    ccf *instance = NULL;
    DDS_ReturnCode_t retcode;
    DDS_InstanceHandle_t instance_handle = DDS_HANDLE_NIL;
    const char *type_name = NULL;
    int count = 0;
    struct DDS_Duration_t send_period = { 1, 0 };
    struct DDS_ContentFilter custom_filter = DDS_ContentFilter_INITIALIZER;

    /* To customize participant QoS, use 
     the configuration file USER_QOS_PROFILES.xml */
    participant = DDS_DomainParticipantFactory_create_participant(
            DDS_TheParticipantFactory, domainId, &DDS_PARTICIPANT_QOS_DEFAULT,
            NULL /* listener */, DDS_STATUS_MASK_NONE);
    if (participant == NULL) {
        printf("create_participant error\n");
        publisher_shutdown(participant);
        return -1;
    }

    /* To customize publisher QoS, use 
     the configuration file USER_QOS_PROFILES.xml */
    publisher = DDS_DomainParticipant_create_publisher(participant,
            &DDS_PUBLISHER_QOS_DEFAULT, NULL /* listener */,
            DDS_STATUS_MASK_NONE);
    if (publisher == NULL) {
        printf("create_publisher error\n");
        publisher_shutdown(participant);
        return -1;
    }

    /* Register type before creating topic */
    type_name = ccfTypeSupport_get_type_name();
    retcode = ccfTypeSupport_register_type(participant, type_name);
    if (retcode != DDS_RETCODE_OK) {
        printf("register_type error %d\n", retcode);
        publisher_shutdown(participant);
        return -1;
    }

    /* To customize topic QoS, use 
     the configuration file USER_QOS_PROFILES.xml */
    topic = DDS_DomainParticipant_create_topic(participant, "Example ccf",
            type_name, &DDS_TOPIC_QOS_DEFAULT, NULL /* listener */,
            DDS_STATUS_MASK_NONE);
    if (topic == NULL) {
        printf("create_topic error\n");
        publisher_shutdown(participant);
        return -1;
    }

    /*------ Start changes for Custom_Content_Filter ------*/
    /* Create and register custom filter */
    custom_filter.compile = custom_filter_compile_function;
    custom_filter.evaluate = custom_filter_evaluate_function;
    custom_filter.finalize = custom_filter_finalize_function;
    custom_filter.filter_data = NULL;

    retcode = DDS_DomainParticipant_register_contentfilter(participant,
            "CustomFilter", &custom_filter);
    if (retcode != DDS_RETCODE_OK) {
        printf("Failed to register custom content filter");
        publisher_shutdown(participant);
        return -1;
    }

    /*------ End changes for Custom_Content_Filter ------*/

    /* To customize data writer QoS, use 
     the configuration file USER_QOS_PROFILES.xml */
    writer = DDS_Publisher_create_datawriter(publisher, topic,
            &DDS_DATAWRITER_QOS_DEFAULT, NULL /* listener */,
            DDS_STATUS_MASK_NONE);
    if (writer == NULL) {
        printf("create_datawriter error\n");
        publisher_shutdown(participant);
        return -1;
    }
    ccf_writer = ccfDataWriter_narrow(writer);
    if (ccf_writer == NULL) {
        printf("DataWriter narrow error\n");
        publisher_shutdown(participant);
        return -1;
    }

    /* Create data sample for writing */

    instance = ccfTypeSupport_create_data_ex(DDS_BOOLEAN_TRUE);

    if (instance == NULL) {
        printf("ccfTypeSupport_create_data error\n");
        publisher_shutdown(participant);
        return -1;
    }

    /* For a data type that has a key, if the same instance is going to be
       written multiple times, initialize the key here
       and register the keyed instance prior to writing */
/*
    instance_handle = ccfDataWriter_register_instance(
        ccf_writer, instance);
*/

    /* Main loop */
    for (count = 0; (sample_count == 0) || (count < sample_count); ++count) {

        printf("Writing ccf, count %d\n", count);

        /* Modify the data to be written here */
        instance->x = count;

        /* Write data */
        retcode = ccfDataWriter_write(ccf_writer, instance, &instance_handle);
        if (retcode != DDS_RETCODE_OK) {
            printf("write error %d\n", retcode);
        }

        NDDS_Utility_sleep(&send_period);
    }

/*
    retcode = ccfDataWriter_unregister_instance(
        ccf_writer, instance, &instance_handle);
    if (retcode != DDS_RETCODE_OK) {
        printf("unregister instance error %d\n", retcode);
    }
*/

    /* Delete data sample */
    retcode = ccfTypeSupport_delete_data_ex(instance, DDS_BOOLEAN_TRUE);
    if (retcode != DDS_RETCODE_OK) {
        printf("ccfTypeSupport_delete_data error %d\n", retcode);
    }

    /* Cleanup and delete delete all entities */
    return publisher_shutdown(participant);
}

#if defined(RTI_WINCE)
int wmain(int argc, wchar_t** argv)
{
    int domainId = 0;
    int sample_count = 0; /* infinite loop */

    if (argc >= 2) {
        domainId = _wtoi(argv[1]);
    }
    if (argc >= 3) {
        sample_count = _wtoi(argv[2]);
    }

    /* Uncomment this to turn on additional logging
     NDDS_Config_Logger_set_verbosity_by_category(
     NDDS_Config_Logger_get_instance(),
     NDDS_CONFIG_LOG_CATEGORY_API,
     NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
     */

    return publisher_main(domainId, sample_count);
}
#elif !(defined(RTI_VXWORKS) && !defined(__RTP__)) && !defined(RTI_PSOS)
int main(int argc, char *argv[]) {
    int domainId = 0;
    int sample_count = 0; /* infinite loop */

    if (argc >= 2) {
        domainId = atoi(argv[1]);
    }
    if (argc >= 3) {
        sample_count = atoi(argv[2]);
    }
    
    /* Uncomment this to turn on additional logging
    NDDS_Config_Logger_set_verbosity_by_category(
        NDDS_Config_Logger_get_instance(),
        NDDS_CONFIG_LOG_CATEGORY_API, 
        NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
    */
        
    return publisher_main(domainId, sample_count);
}
#endif

#ifdef RTI_VX653
const unsigned char* __ctype = NULL;

void usrAppInit ()
{
#ifdef  USER_APPL_INIT
    USER_APPL_INIT; /* for backwards compatibility */
#endif

    /* add application specific code here */
    taskSpawn("pub", RTI_OSAPI_THREAD_PRIORITY_NORMAL, 0x8, 0x150000,
            (FUNCPTR)publisher_main, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

}
#endif
