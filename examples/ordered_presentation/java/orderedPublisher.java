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
/* orderedPublisher.java

 A publication of data of type ordered

 This file is derived from code automatically generated by the rtiddsgen 
 command:

 rtiddsgen -language java -example <arch> .idl

 Example publication of type ordered automatically generated by 
 'rtiddsgen' To test them follow these steps:

 (1) Compile this file and the example subscription.

 (2) Start the subscription with the command
 java orderedSubscriber <domain_id> <sample_count>

 (3) Start the publication with the command
 java orderedPublisher <domain_id> <sample_count>

 (4) [Optional] Specify the list of discovery initial peers and 
 multicast receive addresses via an environment variable or a file 
 (in the current working directory) called NDDS_DISCOVERY_PEERS.  

 You can run any number of publishers and subscribers programs, and can 
 add and remove them dynamically from the domain.

 Example:

 To run the example application on domain <domain_id>:

 Ensure that $(NDDSHOME)/lib/<arch> is on the dynamic library path for
 Java.                       

 On Unix: 
 add $(NDDSHOME)/lib/<arch> to the 'LD_LIBRARY_PATH' environment
 variable

 On Windows:
 add %NDDSHOME%\lib\<arch> to the 'Path' environment variable


 Run the Java applications:

 java -Djava.ext.dirs=$NDDSHOME/class orderedPublisher <domain_id>

 java -Djava.ext.dirs=$NDDSHOME/class orderedSubscriber <domain_id>        



 modification history
 ------------ -------         
 */

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.publication.*;
import com.rti.dds.topic.*;
import com.rti.ndds.config.*;

// ===========================================================================

public class orderedPublisher {
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

        /*
         * Uncomment this to turn on additional logging
         * Logger.get_instance().set_verbosity_by_category(
         * LogCategory.NDDS_CONFIG_LOG_CATEGORY_API,
         * LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
         */

        // --- Run --- //
        publisherMain(domainId, sampleCount);
    }

    // -----------------------------------------------------------------------
    // Private Methods
    // -----------------------------------------------------------------------

    // --- Constructors: -----------------------------------------------------

    private orderedPublisher() {
        super();
    }

    // -----------------------------------------------------------------------

    private static void publisherMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Publisher publisher = null;
        Topic topic = null;
        orderedDataWriter writer = null;

        try {
            // --- Create participant --- //

            /*
             * To customize participant QoS, use the configuration file
             * USER_QOS_PROFILES.xml
             */

            participant = DomainParticipantFactory.TheParticipantFactory
                    .create_participant(domainId,
                            DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                            null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("create_participant error\n");
                return;
            }
            
            /* Start changes for ordered presentation */
            publisher = participant.create_publisher_with_profile(
                    "ordered_Library", "ordered_Profile_subscriber_instance",
                    null, StatusKind.STATUS_MASK_NONE);
            if (publisher == null) {
                System.err.println("create_publisher error\n");
                return;
            }

            /*
             * If you want to change the Publisher's QoS programmatically rather
             * than using the XML file, you will need to add the following lines
             * to your code and comment out the create_publisher call above.
             * 
             * In this case, we set the presentation publish mode ordered in the
             * topic.
             */
            /* Get default publisher QoS to customize */
/*            PublisherQos publisher_qos = new PublisherQos();
            participant.get_default_publisher_qos(publisher_qos);

            publisher_qos.presentation.access_scope = 
                    PresentationQosPolicyAccessScopeKind.TOPIC_PRESENTATION_QOS;
            publisher_qos.presentation.ordered_access = true;

            publisher = participant.create_publisher(publisher_qos,
                    null, StatusKind.STATUS_MASK_NONE);
            if (publisher == null) {
                System.err.println("create_publisher error\n");
                return;
            }
*/
            /* End changes for ordered presentation */
            // --- Create topic --- //

            /* Register type before creating topic */
            String typeName = orderedTypeSupport.get_type_name();
            orderedTypeSupport.register_type(participant, typeName);

            /*
             * To customize topic QoS, use the configuration file
             * USER_QOS_PROFILES.xml
             */

            topic = participant.create_topic("Example ordered", typeName,
                    DomainParticipant.TOPIC_QOS_DEFAULT, null /* listener */,
                    StatusKind.STATUS_MASK_NONE);
            if (topic == null) {
                System.err.println("create_topic error\n");
                return;
            }

            // --- Create writer --- //

            /*
             * To customize data writer QoS, use the configuration file
             * USER_QOS_PROFILES.xml
             */

            writer = (orderedDataWriter) publisher.create_datawriter(topic,
                    Publisher.DATAWRITER_QOS_DEFAULT, null /* listener */,
                    StatusKind.STATUS_MASK_NONE);
            if (writer == null) {
                System.err.println("create_datawriter error\n");
                return;
            }

            /* Start changes for ordered presentation */
            ordered instance0 = new ordered();
            ordered instance1 = new ordered();
            InstanceHandle_t handle0 = InstanceHandle_t.HANDLE_NIL;
            InstanceHandle_t handle1 = InstanceHandle_t.HANDLE_NIL;
            
            // --- Write --- //

            /*
             * For a data type that has a key, if the same instance is going to
             * be written multiple times, initialize the key here and register
             * the keyed instance prior to writing
             */
            instance0.id = 0;
            instance1.id = 1;
            
            handle0 = writer.register_instance(instance0);
            handle1 = writer.register_instance(instance1);
            
            final long sendPeriodMillis = 1000; // 1 second

            for (int count = 0; (sampleCount == 0) || (count < sampleCount);
                    ++count) {
                instance0.value = count;
                instance1.value = count;
                
                System.out.println("writing instance0, value-> " + 
                        instance0.value);
                writer.write(instance0, handle0);

                System.out.println("writing instance1, value-> " +
                        instance1.value);
                writer.write(instance1, handle1);
                
                try {
                    Thread.sleep(sendPeriodMillis);
                } catch (InterruptedException ix) {
                    System.err.println("INTERRUPTED");
                    break;
                }
            }

            writer.unregister_instance(instance0, handle0);
            writer.unregister_instance(instance1, handle1);
            
            /* End changes for ordered presentation */
        } finally {

            // --- Shutdown --- //

            if (participant != null) {
                participant.delete_contained_entities();

                DomainParticipantFactory.TheParticipantFactory
                        .delete_participant(participant);
            }
            /*
             * RTI Connext provides finalize_instance() method for people who
             * want to release memory used by the participant factory singleton.
             * Uncomment the following block of code for clean destruction of
             * the participant factory singleton.
             */
            // DomainParticipantFactory.finalize_instance();
        }
    }
}
