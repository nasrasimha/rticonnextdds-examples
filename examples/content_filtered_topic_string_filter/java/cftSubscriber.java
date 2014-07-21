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

/* cftSubscriber.java

   A publication of data of type cft

   This file is derived from code automatically generated by the rtiddsgen 
   command:

   rtiddsgen -language java -example <arch> .idl

   Example publication of type cft automatically generated by 
   'rtiddsgen' To test them follow these steps:

   (1) Compile this file and the example subscription.

   (2) Start the subscription on the same domain used for with the command
       java cftSubscriber <domain_id> <sample_count>

   (3) Start the publication with the command
       java cftPublisher <domain_id> <sample_count>

   (4) [Optional] Specify the list of discovery initial peers and 
       multicast receive addresses via an environment variable or a file 
       (in the current working directory) called NDDS_DISCOVERY_PEERS. 
       
   You can run any number of publishers and subscribers programs, and can 
   add and remove them dynamically from the domain.
              
                                   
   Example:
        
       To run the example application on domain <domain_id>:
            
       Ensure that $(NDDSHOME)/lib/<arch> is on the dynamic library path for
       Java.                       
       
        On UNIX systems: 
             add $(NDDSHOME)/lib/<arch> to the 'LD_LIBRARY_PATH' environment
             variable
                                         
        On Windows systems:
             add %NDDSHOME%\lib\<arch> to the 'Path' environment variable
                        

       Run the Java applications:
       
        java -Djava.ext.dirs=$NDDSHOME/class cftPublisher <domain_id>

        java -Djava.ext.dirs=$NDDSHOME/class cftSubscriber <domain_id>  
       
       
modification history
------------ -------   
*/

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.stringmatchfilter.StringMatchFilter;
import com.rti.dds.subscription.*;
import com.rti.dds.topic.*;
import com.rti.ndds.config.*;

// ===========================================================================

public class cftSubscriber {
    // -----------------------------------------------------------------------
    // Public Methods
    // -----------------------------------------------------------------------
    
    public static void main(String[] args) {
        // --- Get domain ID --- //
        int domainId = 0;
        if (args.length >= 1) {
            domainId = Integer.valueOf(args[0]).intValue();
        }
        
        // -- Get max loop count; 0 means infinite loop --- //
        int sampleCount = 0;
        if (args.length >= 2) {
            sampleCount = Integer.valueOf(args[1]).intValue();
        }
        
        int sel_cft = 1;
        if (args.length >= 3) {
            sampleCount = Integer.valueOf(args[1]).intValue();
        }

        /* Uncomment this to turn on additional logging
        Logger.get_instance().set_verbosity_by_category(
            LogCategory.NDDS_CONFIG_LOG_CATEGORY_API,
            LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
        */
        
        // --- Run --- //
        subscriberMain(domainId, sampleCount, sel_cft);
    }
    
    
    
    // -----------------------------------------------------------------------
    // Private Methods
    // -----------------------------------------------------------------------
    
    // --- Constructors: -----------------------------------------------------
    
    private cftSubscriber() {
        super();
    }
    
    
    // -----------------------------------------------------------------------
    
    private static void subscriberMain(int domainId, int sampleCount, 
            int sel_cft) {

        DomainParticipant participant = null;
        Subscriber subscriber = null;
        Topic topic = null;
        DataReaderListener listener = null;
        cftDataReader reader = null;

        try {

            // --- Create participant --- //
    
            /* To customize participant QoS, use
               the configuration file
               USER_QOS_PROFILES.xml */
    
            participant = DomainParticipantFactory.TheParticipantFactory.
                create_participant(
                    domainId, DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                    null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("create_participant error\n");
                return;
            }                         

            // --- Create subscriber --- //
    
            /* To customize subscriber QoS, use
               the configuration file USER_QOS_PROFILES.xml */
    
            subscriber = participant.create_subscriber(
                DomainParticipant.SUBSCRIBER_QOS_DEFAULT, null /* listener */,
                StatusKind.STATUS_MASK_NONE);
            if (subscriber == null) {
                System.err.println("create_subscriber error\n");
                return;
            }     
                
            // --- Create topic --- //
        
            /* Register type before creating topic */
            String typeName = cftTypeSupport.get_type_name(); 
            cftTypeSupport.register_type(participant, typeName);
    
            /* To customize topic QoS, use
               the configuration file USER_QOS_PROFILES.xml */
    
            topic = participant.create_topic(
                "Example cft",
                typeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (topic == null) {
                System.err.println("create_topic error\n");
                return;
            }                     
        
            /* For this filter we only allow 1 parameter*/
            String param_list[] = {"SOME_STRING"};
            /* Sequence of parameters for the content filter expression */
            StringSeq parameters = new StringSeq(java.util.Arrays.asList(
                    param_list));
            
            ContentFilteredTopic cft = null;
            if (sel_cft == 1) {
                cft = participant.create_contentfilteredtopic_with_filter(
                        "ContentFilteredTopic", topic, "name MATCH %0", 
                        parameters, 
                        DomainParticipant.STRINGMATCHFILTER_NAME);
                if (cft == null) {
                    System.err.println("create_contentfilteredtopic error\n");
                    return;
                }
            }

            // --- Create reader --- //

            listener = new cftListener();
    
            /* Here we create the reader either using a Content Filtered Topic 
             * or a normal topic */
            if (sel_cft == 1) {
                System.out.print("Using ContentFiltered Topic\n");
                reader = (cftDataReader)
                    subscriber.create_datareader(
                        cft, Subscriber.DATAREADER_QOS_DEFAULT, listener,
                        StatusKind.STATUS_MASK_ALL);
            } else {
                System.out.print("Using Normal Topic\n");
                reader = (cftDataReader)
                    subscriber.create_datareader(
                        topic, Subscriber.DATAREADER_QOS_DEFAULT, listener,
                        StatusKind.STATUS_MASK_ALL);
            }            
            if (reader == null) {
                System.err.println("create_datareader error\n");
                return;
            }               
            
            /* If you want to set the reliability and history QoS settings
             * programmatically rather than using the XML, you will need to add
             * the following lines to your code and comment out the
             * create_datareader calls above.
             */

       /*     
            DataReaderQos datareader_qos = new DataReaderQos();
            subscriber.get_default_datareader_qos(datareader_qos);

            datareader_qos.reliability.kind =
                ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
            datareader_qos.durability.kind = 
                DurabilityQosPolicyKind.TRANSIENT_LOCAL_DURABILITY_QOS;
            datareader_qos.history.kind =
                HistoryQosPolicyKind.KEEP_LAST_HISTORY_QOS;
            datareader_qos.history.depth = 20;

            if (sel_cft == 1) {
                System.out.print("Using ContentFiltered Topic\n");
                reader = (cftDataReader)
                    subscriber.create_datareader(
                        cft, datareader_qos, listener,
                        StatusKind.STATUS_MASK_ALL);
            } else {
                System.out.print("Using Normal Topic\n");
                reader = (cftDataReader)
                    subscriber.create_datareader(
                        topic, datareader_qos, listener,
                        StatusKind.STATUS_MASK_ALL);
            }           
            if (reader == null) {
                System.err.println("create_datareader error\n");
                return;
            }
            
            */
            // --- Wait for data --- //

            System.out.println(">>> Now setting a new filter: "
                    + "name MATCH \"EVEN\"");
            
            if (sel_cft == 1) {
                try { 
                    cft.append_to_expression_parameter(0, "EVEN");
                } catch (Exception e) {
                    System.err.println("append_to_expression_parameter "
                            + "error");
                    return;
                }
            }            
            final long receivePeriodSec = 1;

            for (int count = 0;
                 (sampleCount == 0) || (count < sampleCount);
                 ++count) {
                try {
                    Thread.sleep(receivePeriodSec * 1000);  // in millisec
                } catch (InterruptedException ix) {
                    System.err.println("INTERRUPTED");
                    break;
                }
                
                if (sel_cft == 0) {
                    continue;
                }
                if(count == 10) {
                    System.out.print("\n===========================\n");
                    System.out.print("Changing filter parameters\n");
                    System.out.print("Append 'ODD' filter\n");
                    System.out.print("===========================\n");
                    try {
                        cft.append_to_expression_parameter(0, "ODD");
                    } catch (Exception e) {
                        System.err.println("append_to_expression_parameter "
                                + "error");
                        break;
                    }
                    
                    
                } else if (count == 20) {
                    System.out.print("\n===========================\n");
                    System.out.print("Changing filter parameters\n");
                    System.out.print("Removing 'EVEN' filter\n");
                    System.out.print("===========================\n");
                    
                    try {
                        cft.remove_from_expression_parameter(0, "EVEN");
                    } catch (Exception e) {
                        System.err.println("remove_from_expression_parameter "
                                + "error");
                        break;
                    }
                    
                }     
            }
        } finally {

            // --- Shutdown --- //

            if(participant != null) {
                participant.delete_contained_entities();

                DomainParticipantFactory.TheParticipantFactory.
                    delete_participant(participant);
            }
            /* RTI Connext provides the finalize_instance()
               method for users who want to release memory used by the
               participant factory singleton. Uncomment the following block of
               code for clean destruction of the participant factory
               singleton. */
            //DomainParticipantFactory.finalize_instance();
        }
    }
    
    // -----------------------------------------------------------------------
    // Private Types
    // -----------------------------------------------------------------------
    
    // =======================================================================
    
    private static class cftListener extends DataReaderAdapter {
            
        cftSeq _dataSeq = new cftSeq();
        SampleInfoSeq _infoSeq = new SampleInfoSeq();

        public void on_data_available(DataReader reader) {
            cftDataReader cftReader =
                (cftDataReader)reader;
            
            try {
                cftReader.take(
                    _dataSeq, _infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

                for(int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo)_infoSeq.get(i);

                    if (info.valid_data) {
                        System.out.println(
                            ((cft)_dataSeq.get(i)).toString("Received",0));


                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
                cftReader.return_loan(_dataSeq, _infoSeq);
            }
        }
    }
}


        