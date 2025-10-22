/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.constants.http;

import jakarta.ws.rs.core.MediaType;

/**
 * <p>
 *     List of well know media types representations defined by various sources.
 *     The media types are grouped by main types into {@link Application}, {@link Audio},
 *     {@link Font}, {@link Haptics}, {@link Image}, {@link Message}, {@link Model},
 *     {@link Multipart}, {@link Text}, and {@link Video} subclasses.
 * </p>
 * <p>
 *     Subtracted from {@code https://www.iana.org/assignments/media-types/media-types.xml}.
 * </p>
 */
public final class MediaTypes {

    /**
     * Application type media subtypes.
     */
    public static class Application {
        /**
         * A {@code String} constant representing {@value #APPLICATION_1D_INTERLEAVED_PARITYFEC} media
         * type defined by RFC 6015.
         */
        public static final String APPLICATION_1D_INTERLEAVED_PARITYFEC =
                "application/1d-interleaved-parityfec";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_1D_INTERLEAVED_PARITYFEC} media
         * type defined by RFC 6015.
         */
        public static final MediaType APPLICATION_1D_INTERLEAVED_PARITYFEC_TYPE =
                new MediaType("application", "1d-interleaved-parityfec");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ACE_GROUPCOMM_CBOR} media
         * type defined by RFC 9594.
         */
        public static final String APPLICATION_ACE_GROUPCOMM_CBOR =
                "application/ace-groupcomm+cbor";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ACE_GROUPCOMM_CBOR} media
         * type defined by RFC 9594.
         */
        public static final MediaType APPLICATION_ACE_GROUPCOMM_CBOR_TYPE =
                new MediaType("application", "ace-groupcomm+cbor");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ACE_TRL_CBOR} media
         * type defined by RFC 9770.
         */
        public static final String APPLICATION_ACE_TRL_CBOR =
                "application/ace-trl+cbor";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ACE_TRL_CBOR} media
         * type defined by RFC 9770.
         */
        public static final MediaType APPLICATION_ACE_TRL_CBOR_TYPE =
                new MediaType("application", "ace-trl+cbor");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ACE_CBOR} media
         * type defined by RFC 9200.
         */
        public static final String APPLICATION_ACE_CBOR =
                "application/ace+cbor";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ACE_CBOR} media
         * type defined by RFC 9200.
         */
        public static final MediaType APPLICATION_ACE_CBOR_TYPE =
                new MediaType("application", "ace+cbor");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ACE_JSON} media
         * type defined by RFC 9431.
         */
        public static final String APPLICATION_ACE_JSON =
                "application/ace+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ACE_JSON} media
         * type defined by RFC 9431.
         */
        public static final MediaType APPLICATION_ACE_JSON_TYPE =
                new MediaType("application", "ace+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_AIF_CBOR} media
         * type defined by RFC 9237.
         */
        public static final String APPLICATION_AIF_CBOR =
                "application/aif+cbor";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_AIF_CBOR} media
         * type defined by RFC 9237.
         */
        public static final MediaType APPLICATION_AIF_CBOR_TYPE =
                new MediaType("application", "aif+cbor");

        /**
         * A {@code String} constant representing {@value #APPLICATION_AIF_JSON} media
         * type defined by RFC 9237.
         */
        public static final String APPLICATION_AIF_JSON =
                "application/aif+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_AIF_JSON} media
         * type defined by RFC 9237.
         */
        public static final MediaType APPLICATION_AIF_JSON_TYPE =
                new MediaType("application", "aif+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_CDNI_JSON} media
         * type defined by RFC 9241.
         */
        public static final String APPLICATION_ALTO_CDNI_JSON =
                "application/alto-cdni+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_CDNI_JSON} media
         * type defined by RFC 9241.
         */
        public static final MediaType APPLICATION_ALTO_CDNI_JSON_TYPE =
                new MediaType("application", "alto-cdni+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_CDNIFILTER_JSON} media
         * type defined by RFC 9241.
         */
        public static final String APPLICATION_ALTO_CDNIFILTER_JSON =
                "application/alto-cdnifilter+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_CDNIFILTER_JSON} media
         * type defined by RFC 9241.
         */
        public static final MediaType APPLICATION_ALTO_CDNIFILTER_JSON_TYPE =
                new MediaType("application", "alto-cdnifilter+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_COSTMAP_JSON} media
         * type defined by RFC 7285.
         */
        public static final String APPLICATION_ALTO_COSTMAP_JSON =
                "application/alto-costmap+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_COSTMAP_JSON} media
         * type defined by RFC 7285.
         */
        public static final MediaType APPLICATION_ALTO_COSTMAP_JSON_TYPE =
                new MediaType("application", "alto-costmap+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_COSTMAPFILTER_JSON} media
         * type defined by RFC 7285.
         */
        public static final String APPLICATION_ALTO_COSTMAPFILTER_JSON =
                "application/alto-costmapfilter+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_COSTMAPFILTER_JSON} media
         * type defined by RFC 7285.
         */
        public static final MediaType APPLICATION_ALTO_COSTMAPFILTER_JSON_TYPE =
                new MediaType("application", "alto-costmapfilter+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_DIRECTORY_JSON} media
         * type defined by RFC 7285.
         */
        public static final String APPLICATION_ALTO_DIRECTORY_JSON =
                "application/alto-directory+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_DIRECTORY_JSON} media
         * type defined by RFC 7285.
         */
        public static final MediaType APPLICATION_ALTO_DIRECTORY_JSON_TYPE =
                new MediaType("application", "alto-directory+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_ENDPOINTPROP_JSON} media
         * type defined by RFC 7285.
         */
        public static final String APPLICATION_ALTO_ENDPOINTPROP_JSON =
                "application/alto-endpointprop+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_ENDPOINTPROP_JSON} media
         * type defined by RFC 7285.
         */
        public static final MediaType APPLICATION_ALTO_ENDPOINTPROP_JSON_TYPE =
                new MediaType("application", "alto-endpointprop+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_ENDPOINTPROPPARAMS_JSON} media
         * type defined by RFC 7285.
         */
        public static final String APPLICATION_ALTO_ENDPOINTPROPPARAMS_JSON =
                "application/alto-endpointpropparams+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_ENDPOINTPROPPARAMS_JSON} media
         * type defined by RFC 7285.
         */
        public static final MediaType APPLICATION_ALTO_ENDPOINTPROPPARAMS_JSON_TYPE =
                new MediaType("application", "alto-endpointpropparams+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_ENDPOINTCOST_JSON} media
         * type defined by RFC 7285.
         */
        public static final String APPLICATION_ALTO_ENDPOINTCOST_JSON =
                "application/alto-endpointcost+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_ENDPOINTCOST_JSON} media
         * type defined by RFC 7285.
         */
        public static final MediaType APPLICATION_ALTO_ENDPOINTCOST_JSON_TYPE =
                new MediaType("application", "alto-endpointcost+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_ENDPOINTCOSTPARAMS_JSON} media
         * type defined by RFC 7285.
         */
        public static final String APPLICATION_ALTO_ENDPOINTCOSTPARAMS_JSON =
                "application/alto-endpointcostparams+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_ENDPOINTCOSTPARAMS_JSON} media
         * type defined by RFC 7285.
         */
        public static final MediaType APPLICATION_ALTO_ENDPOINTCOSTPARAMS_JSON_TYPE =
                new MediaType("application", "alto-endpointcostparams+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_ERROR_JSON} media
         * type defined by RFC 7285.
         */
        public static final String APPLICATION_ALTO_ERROR_JSON =
                "application/alto-error+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_ERROR_JSON} media
         * type defined by RFC 7285.
         */
        public static final MediaType APPLICATION_ALTO_ERROR_JSON_TYPE =
                new MediaType("application", "alto-error+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_NETWORKMAPFILTER_JSON} media
         * type defined by RFC 7285.
         */
        public static final String APPLICATION_ALTO_NETWORKMAPFILTER_JSON =
                "application/alto-networkmapfilter+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_NETWORKMAPFILTER_JSON} media
         * type defined by RFC 7285.
         */
        public static final MediaType APPLICATION_ALTO_NETWORKMAPFILTER_JSON_TYPE =
                new MediaType("application", "alto-networkmapfilter+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_NETWORKMAP_JSON} media
         * type defined by RFC 7285.
         */
        public static final String APPLICATION_ALTO_NETWORKMAP_JSON =
                "application/alto-networkmap+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_NETWORKMAP_JSON} media
         * type defined by RFC 7285.
         */
        public static final MediaType APPLICATION_ALTO_NETWORKMAP_JSON_TYPE =
                new MediaType("application", "alto-networkmap+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_PROPMAP_JSON} media
         * type defined by RFC 9240.
         */
        public static final String APPLICATION_ALTO_PROPMAP_JSON =
                "application/alto-propmap+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_PROPMAP_JSON} media
         * type defined by RFC 9240.
         */
        public static final MediaType APPLICATION_ALTO_PROPMAP_JSON_TYPE =
                new MediaType("application", "alto-propmap+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_PROPMAPPARAMS_JSON} media
         * type defined by RFC 9240.
         */
        public static final String APPLICATION_ALTO_PROPMAPPARAMS_JSON =
                "application/alto-propmapparams+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_PROPMAPPARAMS_JSON} media
         * type defined by RFC 9240.
         */
        public static final MediaType APPLICATION_ALTO_PROPMAPPARAMS_JSON_TYPE =
                new MediaType("application", "alto-propmapparams+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_TIPS_JSON} media
         * type defined by RFC 9569.
         */
        public static final String APPLICATION_ALTO_TIPS_JSON =
                "application/alto-tips+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_TIPS_JSON} media
         * type defined by RFC 9569.
         */
        public static final MediaType APPLICATION_ALTO_TIPS_JSON_TYPE =
                new MediaType("application", "alto-tips+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_TIPSPARAMS_JSON} media
         * type defined by RFC 9569.
         */
        public static final String APPLICATION_ALTO_TIPSPARAMS_JSON =
                "application/alto-tipsparams+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_TIPSPARAMS_JSON} media
         * type defined by RFC 9569.
         */
        public static final MediaType APPLICATION_ALTO_TIPSPARAMS_JSON_TYPE =
                new MediaType("application", "alto-tipsparams+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_UPDATESTREAMCONTROL_JSON} media
         * type defined by RFC 8895.
         */
        public static final String APPLICATION_ALTO_UPDATESTREAMCONTROL_JSON =
                "application/alto-updatestreamcontrol+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_UPDATESTREAMCONTROL_JSON} media
         * type defined by RFC 8895.
         */
        public static final MediaType APPLICATION_ALTO_UPDATESTREAMCONTROL_JSON_TYPE =
                new MediaType("application", "alto-updatestreamcontrol+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ALTO_UPDATESTREAMPARAMS_JSON} media
         * type defined by RFC 8895.
         */
        public static final String APPLICATION_ALTO_UPDATESTREAMPARAMS_JSON =
                "application/alto-updatestreamparams+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ALTO_UPDATESTREAMPARAMS_JSON} media
         * type defined by RFC 8895.
         */
        public static final MediaType APPLICATION_ALTO_UPDATESTREAMPARAMS_JSON_TYPE =
                new MediaType("application", "alto-updatestreamparams+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_AT_JWT} media
         * type defined by RFC 9068.
         */
        public static final String APPLICATION_AT_JWT =
                "application/at+jwt";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_AT_JWT} media
         * type defined by RFC 9068.
         */
        public static final MediaType APPLICATION_AT_JWT_TYPE =
                new MediaType("application", "at+jwt");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ATOM_XML} media
         * type defined by RFC 4287, and RFC 5023.
         */
        public static final String APPLICATION_ATOM_XML =
                "application/atom+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ATOM_XML} media
         * type defined by RFC 4287, and RFC 5023.
         */
        public static final MediaType APPLICATION_ATOM_XML_TYPE =
                new MediaType("application", "atom+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ATOMCAT_XML} media
         * type defined by RFC 5023.
         */
        public static final String APPLICATION_ATOMCAT_XML =
                "application/atomcat+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ATOMCAT_XML} media
         * type defined by RFC 5023.
         */
        public static final MediaType APPLICATION_ATOMCAT_XML_TYPE =
                new MediaType("application", "atomcat+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ATOMDELETED_XML} media
         * type defined by RFC 6721.
         */
        public static final String APPLICATION_ATOMDELETED_XML =
                "application/atomdeleted+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ATOMDELETED_XML} media
         * type defined by RFC 6721.
         */
        public static final MediaType APPLICATION_ATOMDELETED_XML_TYPE =
                new MediaType("application", "atomdeleted+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ATOMSVC_XML} media
         * type defined by RFC 5023.
         */
        public static final String APPLICATION_ATOMSVC_XML =
                "application/atomsvc+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ATOMSVC_XML} media
         * type defined by RFC 5023.
         */
        public static final MediaType APPLICATION_ATOMSVC_XML_TYPE =
                new MediaType("application", "atomsvc+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_AUTH_POLICY_XML} media
         * type defined by RFC 4745.
         */
        public static final String APPLICATION_AUTH_POLICY_XML =
                "application/auth-policy+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_AUTH_POLICY_XML} media
         * type defined by RFC 4745.
         */
        public static final MediaType APPLICATION_AUTH_POLICY_XML_TYPE =
                new MediaType("application", "auth-policy+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_BATCH_SMTP} media
         * type defined by RFC 2442.
         */
        public static final String APPLICATION_BATCH_SMTP =
                "application/batch-SMTP";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_BATCH_SMTP} media
         * type defined by RFC 2442.
         */
        public static final MediaType APPLICATION_BATCH_SMTP_TYPE =
                new MediaType("application", "batch-SMTP");

        /**
         * A {@code String} constant representing {@value #APPLICATION_BEEP_XML} media
         * type defined by RFC 3080.
         */
        public static final String APPLICATION_BEEP_XML =
                "application/beep+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_BEEP_XML} media
         * type defined by RFC 3080.
         */
        public static final MediaType APPLICATION_BEEP_XML_TYPE =
                new MediaType("application", "beep+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CALENDAR_JSON} media
         * type defined by RFC 7265.
         */
        public static final String APPLICATION_CALENDAR_JSON =
                "application/calendar+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CALENDAR_JSON} media
         * type defined by RFC 7265.
         */
        public static final MediaType APPLICATION_CALENDAR_JSON_TYPE =
                new MediaType("application", "calendar+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CALENDAR_XML} media
         * type defined by RFC 6321.
         */
        public static final String APPLICATION_CALENDAR_XML =
                "application/calendar+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CALENDAR_XML} media
         * type defined by RFC 6321.
         */
        public static final MediaType APPLICATION_CALENDAR_XML_TYPE =
                new MediaType("application", "calendar+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CALL_COMPLETION} media
         * type defined by RFC 6910.
         */
        public static final String APPLICATION_CALL_COMPLETION =
                "application/call-completion";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CALL_COMPLETION} media
         * type defined by RFC 6910.
         */
        public static final MediaType APPLICATION_CALL_COMPLETION_TYPE =
                new MediaType("application", "call-completion");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CALS_1840} media
         * type defined by RFC 1895.
         */
        public static final String APPLICATION_CALS_1840 =
                "application/CALS-1840";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CALS_1840} media
         * type defined by RFC 1895.
         */
        public static final MediaType APPLICATION_CALS_1840_TYPE =
                new MediaType("application", "CALS-1840");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CAPTIVE_JSON} media
         * type defined by RFC 8908.
         */
        public static final String APPLICATION_CAPTIVE_JSON =
                "application/captive+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CAPTIVE_JSON} media
         * type defined by RFC 8908.
         */
        public static final MediaType APPLICATION_CAPTIVE_JSON_TYPE =
                new MediaType("application", "captive+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CBOR} media
         * type defined by RFC 8949.
         */
        public static final String APPLICATION_CBOR =
                "application/cbor";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CBOR} media
         * type defined by RFC 8949.
         */
        public static final MediaType APPLICATION_CBOR_TYPE =
                new MediaType("application", "cbor");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CBOR_SEQ} media
         * type defined by RFC 8742.
         */
        public static final String APPLICATION_CBOR_SEQ =
                "application/cbor-seq";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CBOR_SEQ} media
         * type defined by RFC 8742.
         */
        public static final MediaType APPLICATION_CBOR_SEQ_TYPE =
                new MediaType("application", "cbor-seq");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CCMP_XML} media
         * type defined by RFC 6503.
         */
        public static final String APPLICATION_CCMP_XML =
                "application/ccmp+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CCMP_XML} media
         * type defined by RFC 6503.
         */
        public static final MediaType APPLICATION_CCMP_XML_TYPE =
                new MediaType("application", "ccmp+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CCXML_XML} media
         * type defined by RFC 4267.
         */
        public static final String APPLICATION_CCXML_XML =
                "application/ccxml+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CCXML_XML} media
         * type defined by RFC 4267.
         */
        public static final MediaType APPLICATION_CCXML_XML_TYPE =
                new MediaType("application", "ccxml+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CDMI_CAPABILITY} media
         * type defined by RFC 6208.
         */
        public static final String APPLICATION_CDMI_CAPABILITY =
                "application/cdmi-capability";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CDMI_CAPABILITY} media
         * type defined by RFC 6208.
         */
        public static final MediaType APPLICATION_CDMI_CAPABILITY_TYPE =
                new MediaType("application", "cdmi-capability");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CDMI_CONTAINER} media
         * type defined by RFC 6208.
         */
        public static final String APPLICATION_CDMI_CONTAINER =
                "application/cdmi-container";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CDMI_CONTAINER} media
         * type defined by RFC 6208.
         */
        public static final MediaType APPLICATION_CDMI_CONTAINER_TYPE =
                new MediaType("application", "cdmi-container");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CDMI_DOMAIN} media
         * type defined by RFC 6208.
         */
        public static final String APPLICATION_CDMI_DOMAIN =
                "application/cdmi-domain";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CDMI_DOMAIN} media
         * type defined by RFC 6208.
         */
        public static final MediaType APPLICATION_CDMI_DOMAIN_TYPE =
                new MediaType("application", "cdmi-domain");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CDMI_OBJECT} media
         * type defined by RFC 6208.
         */
        public static final String APPLICATION_CDMI_OBJECT =
                "application/cdmi-object";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CDMI_OBJECT} media
         * type defined by RFC 6208.
         */
        public static final MediaType APPLICATION_CDMI_OBJECT_TYPE =
                new MediaType("application", "cdmi-object");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CDMI_QUEUE} media
         * type defined by RFC 6208.
         */
        public static final String APPLICATION_CDMI_QUEUE =
                "application/cdmi-queue";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CDMI_QUEUE} media
         * type defined by RFC 6208.
         */
        public static final MediaType APPLICATION_CDMI_QUEUE_TYPE =
                new MediaType("application", "cdmi-queue");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CDNI} media
         * type defined by RFC 7736.
         */
        public static final String APPLICATION_CDNI =
                "application/cdni";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CDNI} media
         * type defined by RFC 7736.
         */
        public static final MediaType APPLICATION_CDNI_TYPE =
                new MediaType("application", "cdni");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CELLML_XML} media
         * type defined by RFC 4708.
         */
        public static final String APPLICATION_CELLML_XML =
                "application/cellml+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CELLML_XML} media
         * type defined by RFC 4708.
         */
        public static final MediaType APPLICATION_CELLML_XML_TYPE =
                new MediaType("application", "cellml+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CFW} media
         * type defined by RFC 6230.
         */
        public static final String APPLICATION_CFW =
                "application/cfw";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CFW} media
         * type defined by RFC 6230.
         */
        public static final MediaType APPLICATION_CFW_TYPE =
                new MediaType("application", "cfw");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CID_EDHOC_CBOR_SEQ} media
         * type defined by RFC 9528.
         */
        public static final String APPLICATION_CID_EDHOC_CBOR_SEQ =
                "application/cid-edhoc+cbor-seq";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CID_EDHOC_CBOR_SEQ} media
         * type defined by RFC 9528.
         */
        public static final MediaType APPLICATION_CID_EDHOC_CBOR_SEQ_TYPE =
                new MediaType("application", "cid-edhoc+cbor-seq");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CLUE_INFO_XML} media
         * type defined by RFC 8846.
         */
        public static final String APPLICATION_CLUE_INFO_XML =
                "application/clue_info+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CLUE_INFO_XML} media
         * type defined by RFC 8846.
         */
        public static final MediaType APPLICATION_CLUE_INFO_XML_TYPE =
                new MediaType("application", "clue_info+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CLUE_XML} media
         * type defined by RFC 8847.
         */
        public static final String APPLICATION_CLUE_XML =
                "application/clue+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CLUE_XML} media
         * type defined by RFC 8847.
         */
        public static final MediaType APPLICATION_CLUE_XML_TYPE =
                new MediaType("application", "clue+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CMS} media
         * type defined by RFC 7193.
         */
        public static final String APPLICATION_CMS =
                "application/cms";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CMS} media
         * type defined by RFC 7193.
         */
        public static final MediaType APPLICATION_CMS_TYPE =
                new MediaType("application", "cms");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CNRP_XML} media
         * type defined by RFC 3367.
         */
        public static final String APPLICATION_CNRP_XML =
                "application/cnrp+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CNRP_XML} media
         * type defined by RFC 3367.
         */
        public static final MediaType APPLICATION_CNRP_XML_TYPE =
                new MediaType("application", "cnrp+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_COAP_EAP} media
         * type defined by RFC 9820.
         */
        public static final String APPLICATION_COAP_EAP =
                "application/coap-eap";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_COAP_EAP} media
         * type defined by RFC 9820.
         */
        public static final MediaType APPLICATION_COAP_EAP_TYPE =
                new MediaType("application", "coap-eap");

        /**
         * A {@code String} constant representing {@value #APPLICATION_COAP_GROUP_JSON} media
         * type defined by RFC 7390.
         */
        public static final String APPLICATION_COAP_GROUP_JSON =
                "application/coap-group+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_COAP_GROUP_JSON} media
         * type defined by RFC 7390.
         */
        public static final MediaType APPLICATION_COAP_GROUP_JSON_TYPE =
                new MediaType("application", "coap-group+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_COAP_PAYLOAD} media
         * type defined by RFC 8075.
         */
        public static final String APPLICATION_COAP_PAYLOAD =
                "application/coap-payload";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_COAP_PAYLOAD} media
         * type defined by RFC 8075.
         */
        public static final MediaType APPLICATION_COAP_PAYLOAD_TYPE =
                new MediaType("application", "coap-payload");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CONCISE_PROBLEM_DETAILS_CBOR} media
         * type defined by RFC 9290, Section 6.3.
         */
        public static final String APPLICATION_CONCISE_PROBLEM_DETAILS_CBOR =
                "application/concise-problem-details+cbor";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CONCISE_PROBLEM_DETAILS_CBOR} media
         * type defined by RFC 9290, Section 6.3.
         */
        public static final MediaType APPLICATION_CONCISE_PROBLEM_DETAILS_CBOR_TYPE =
                new MediaType("application", "concise-problem-details+cbor");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CONFERENCE_INFO_XML} media
         * type defined by RFC 4575.
         */
        public static final String APPLICATION_CONFERENCE_INFO_XML =
                "application/conference-info+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CONFERENCE_INFO_XML} media
         * type defined by RFC 4575.
         */
        public static final MediaType APPLICATION_CONFERENCE_INFO_XML_TYPE =
                new MediaType("application", "conference-info+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CPL_XML} media
         * type defined by RFC 3880.
         */
        public static final String APPLICATION_CPL_XML =
                "application/cpl+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CPL_XML} media
         * type defined by RFC 3880.
         */
        public static final MediaType APPLICATION_CPL_XML_TYPE =
                new MediaType("application", "cpl+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_COSE} media
         * type defined by RFC 9052.
         */
        public static final String APPLICATION_COSE =
                "application/cose";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_COSE} media
         * type defined by RFC 9052.
         */
        public static final MediaType APPLICATION_COSE_TYPE =
                new MediaType("application", "cose");

        /**
         * A {@code String} constant representing {@value #APPLICATION_COSE_KEY} media
         * type defined by RFC 9052.
         */
        public static final String APPLICATION_COSE_KEY =
                "application/cose-key";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_COSE_KEY} media
         * type defined by RFC 9052.
         */
        public static final MediaType APPLICATION_COSE_KEY_TYPE =
                new MediaType("application", "cose-key");

        /**
         * A {@code String} constant representing {@value #APPLICATION_COSE_KEY_SET} media
         * type defined by RFC 9052.
         */
        public static final String APPLICATION_COSE_KEY_SET =
                "application/cose-key-set";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_COSE_KEY_SET} media
         * type defined by RFC 9052.
         */
        public static final MediaType APPLICATION_COSE_KEY_SET_TYPE =
                new MediaType("application", "cose-key-set");

        /**
         * A {@code String} constant representing {@value #APPLICATION_COSE_X509} media
         * type defined by RFC 9360.
         */
        public static final String APPLICATION_COSE_X509 =
                "application/cose-x509";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_COSE_X509} media
         * type defined by RFC 9360.
         */
        public static final MediaType APPLICATION_COSE_X509_TYPE =
                new MediaType("application", "cose-x509");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CSRATTRS} media
         * type defined by RFC 7030.
         */
        public static final String APPLICATION_CSRATTRS =
                "application/csrattrs";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CSRATTRS} media
         * type defined by RFC 7030.
         */
        public static final MediaType APPLICATION_CSRATTRS_TYPE =
                new MediaType("application", "csrattrs");

        /**
         * A {@code String} constant representing {@value #APPLICATION_CWT} media
         * type defined by RFC 8392.
         */
        public static final String APPLICATION_CWT =
                "application/cwt";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_CWT} media
         * type defined by RFC 8392.
         */
        public static final MediaType APPLICATION_CWT_TYPE =
                new MediaType("application", "cwt");

        /**
         * A {@code String} constant representing {@value #APPLICATION_DAVMOUNT_XML} media
         * type defined by RFC 4709.
         */
        public static final String APPLICATION_DAVMOUNT_XML =
                "application/davmount+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_DAVMOUNT_XML} media
         * type defined by RFC 4709.
         */
        public static final MediaType APPLICATION_DAVMOUNT_XML_TYPE =
                new MediaType("application", "davmount+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_DIALOG_INFO_XML} media
         * type defined by RFC 4235.
         */
        public static final String APPLICATION_DIALOG_INFO_XML =
                "application/dialog-info+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_DIALOG_INFO_XML} media
         * type defined by RFC 4235.
         */
        public static final MediaType APPLICATION_DIALOG_INFO_XML_TYPE =
                new MediaType("application", "dialog-info+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_DICOM} media
         * type defined by RFC 3240.
         */
        public static final String APPLICATION_DICOM =
                "application/dicom";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_DICOM} media
         * type defined by RFC 3240.
         */
        public static final MediaType APPLICATION_DICOM_TYPE =
                new MediaType("application", "dicom");

        /**
         * A {@code String} constant representing {@value #APPLICATION_DNS} media
         * type defined by RFC 4027.
         */
        public static final String APPLICATION_DNS =
                "application/dns";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_DNS} media
         * type defined by RFC 4027.
         */
        public static final MediaType APPLICATION_DNS_TYPE =
                new MediaType("application", "dns");

        /**
         * A {@code String} constant representing {@value #APPLICATION_DNS_JSON} media
         * type defined by RFC 8427.
         */
        public static final String APPLICATION_DNS_JSON =
                "application/dns+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_DNS_JSON} media
         * type defined by RFC 8427.
         */
        public static final MediaType APPLICATION_DNS_JSON_TYPE =
                new MediaType("application", "dns+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_DNS_MESSAGE} media
         * type defined by RFC 8484.
         */
        public static final String APPLICATION_DNS_MESSAGE =
                "application/dns-message";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_DNS_MESSAGE} media
         * type defined by RFC 8484.
         */
        public static final MediaType APPLICATION_DNS_MESSAGE_TYPE =
                new MediaType("application", "dns-message");

        /**
         * A {@code String} constant representing {@value #APPLICATION_DOTS_CBOR} media
         * type defined by RFC 9132.
         */
        public static final String APPLICATION_DOTS_CBOR =
                "application/dots+cbor";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_DOTS_CBOR} media
         * type defined by RFC 9132.
         */
        public static final MediaType APPLICATION_DOTS_CBOR_TYPE =
                new MediaType("application", "dots+cbor");

        /**
         * A {@code String} constant representing {@value #APPLICATION_DPOP_JWT} media
         * type defined by RFC 9449.
         */
        public static final String APPLICATION_DPOP_JWT =
                "application/dpop+jwt";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_DPOP_JWT} media
         * type defined by RFC 9449.
         */
        public static final MediaType APPLICATION_DPOP_JWT_TYPE =
                new MediaType("application", "dpop+jwt");

        /**
         * A {@code String} constant representing {@value #APPLICATION_DSKPP_XML} media
         * type defined by RFC 6063.
         */
        public static final String APPLICATION_DSKPP_XML =
                "application/dskpp+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_DSKPP_XML} media
         * type defined by RFC 6063.
         */
        public static final MediaType APPLICATION_DSKPP_XML_TYPE =
                new MediaType("application", "dskpp+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_DSSC_DER} media
         * type defined by RFC 5698.
         */
        public static final String APPLICATION_DSSC_DER =
                "application/dssc+der";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_DSSC_DER} media
         * type defined by RFC 5698.
         */
        public static final MediaType APPLICATION_DSSC_DER_TYPE =
                new MediaType("application", "dssc+der");

        /**
         * A {@code String} constant representing {@value #APPLICATION_DSSC_XML} media
         * type defined by RFC 5698.
         */
        public static final String APPLICATION_DSSC_XML =
                "application/dssc+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_DSSC_XML} media
         * type defined by RFC 5698.
         */
        public static final MediaType APPLICATION_DSSC_XML_TYPE =
                new MediaType("application", "dssc+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_DVCS} media
         * type defined by RFC 3029.
         */
        public static final String APPLICATION_DVCS =
                "application/dvcs";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_DVCS} media
         * type defined by RFC 3029.
         */
        public static final MediaType APPLICATION_DVCS_TYPE =
                new MediaType("application", "dvcs");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EAT_CWT} media
         * type defined by RFC 9782.
         */
        public static final String APPLICATION_EAT_CWT =
                "application/eat+cwt";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EAT_CWT} media
         * type defined by RFC 9782.
         */
        public static final MediaType APPLICATION_EAT_CWT_TYPE =
                new MediaType("application", "eat+cwt");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EAT_JWT} media
         * type defined by RFC 9782.
         */
        public static final String APPLICATION_EAT_JWT =
                "application/eat+jwt";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EAT_JWT} media
         * type defined by RFC 9782.
         */
        public static final MediaType APPLICATION_EAT_JWT_TYPE =
                new MediaType("application", "eat+jwt");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EAT_BUN_CBOR} media
         * type defined by RFC 9782.
         */
        public static final String APPLICATION_EAT_BUN_CBOR =
                "application/eat-bun+cbor";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EAT_BUN_CBOR} media
         * type defined by RFC 9782.
         */
        public static final MediaType APPLICATION_EAT_BUN_CBOR_TYPE =
                new MediaType("application", "eat-bun+cbor");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EAT_BUN_JSON} media
         * type defined by RFC 9782.
         */
        public static final String APPLICATION_EAT_BUN_JSON =
                "application/eat-bun+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EAT_BUN_JSON} media
         * type defined by RFC 9782.
         */
        public static final MediaType APPLICATION_EAT_BUN_JSON_TYPE =
                new MediaType("application", "eat-bun+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EAT_UCS_CBOR} media
         * type defined by RFC 9782.
         */
        public static final String APPLICATION_EAT_UCS_CBOR =
                "application/eat-ucs+cbor";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EAT_UCS_CBOR} media
         * type defined by RFC 9782.
         */
        public static final MediaType APPLICATION_EAT_UCS_CBOR_TYPE =
                new MediaType("application", "eat-ucs+cbor");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EAT_UCS_JSON} media
         * type defined by RFC 9782.
         */
        public static final String APPLICATION_EAT_UCS_JSON =
                "application/eat-ucs+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EAT_UCS_JSON} media
         * type defined by RFC 9782.
         */
        public static final MediaType APPLICATION_EAT_UCS_JSON_TYPE =
                new MediaType("application", "eat-ucs+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ECMASCRIPT} media
         * type defined by RFC 4329, and RFC 9239.
         */
        public static final String APPLICATION_ECMASCRIPT =
                "application/ecmascript";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ECMASCRIPT} media
         * type defined by RFC 4329, and RFC 9239.
         */
        public static final MediaType APPLICATION_ECMASCRIPT_TYPE =
                new MediaType("application", "ecmascript");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EDHOC_CBOR_SEQ} media
         * type defined by RFC 9528.
         */
        public static final String APPLICATION_EDHOC_CBOR_SEQ =
                "application/edhoc+cbor-seq";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EDHOC_CBOR_SEQ} media
         * type defined by RFC 9528.
         */
        public static final MediaType APPLICATION_EDHOC_CBOR_SEQ_TYPE =
                new MediaType("application", "edhoc+cbor-seq");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EDI_CONSENT} media
         * type defined by RFC 1767.
         */
        public static final String APPLICATION_EDI_CONSENT =
                "application/EDI-consent";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EDI_CONSENT} media
         * type defined by RFC 1767.
         */
        public static final MediaType APPLICATION_EDI_CONSENT_TYPE =
                new MediaType("application", "EDI-consent");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EDIFACT} media
         * type defined by RFC 1767.
         */
        public static final String APPLICATION_EDIFACT =
                "application/EDIFACT";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EDIFACT} media
         * type defined by RFC 1767.
         */
        public static final MediaType APPLICATION_EDIFACT_TYPE =
                new MediaType("application", "EDIFACT");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EDI_X12} media
         * type defined by RFC 1767.
         */
        public static final String APPLICATION_EDI_X12 =
                "application/EDI-X12";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EDI_X12} media
         * type defined by RFC 1767.
         */
        public static final MediaType APPLICATION_EDI_X12_TYPE =
                new MediaType("application", "EDI-X12");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_CAP_XML} media
         * type defined by RFC 8876.
         */
        public static final String APPLICATION_EMERGENCYCALLDATA_CAP_XML =
                "application/EmergencyCallData.cap+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_CAP_XML} media
         * type defined by RFC 8876.
         */
        public static final MediaType APPLICATION_EMERGENCYCALLDATA_CAP_XML_TYPE =
                new MediaType("application", "EmergencyCallData.cap+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_COMMENT_XML} media
         * type defined by RFC 7852.
         */
        public static final String APPLICATION_EMERGENCYCALLDATA_COMMENT_XML =
                "application/EmergencyCallData.Comment+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_COMMENT_XML} media
         * type defined by RFC 7852.
         */
        public static final MediaType APPLICATION_EMERGENCYCALLDATA_COMMENT_XML_TYPE =
                new MediaType("application", "EmergencyCallData.Comment+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_CONTROL_XML} media
         * type defined by RFC 8147.
         */
        public static final String APPLICATION_EMERGENCYCALLDATA_CONTROL_XML =
                "application/EmergencyCallData.Control+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_CONTROL_XML} media
         * type defined by RFC 8147.
         */
        public static final MediaType APPLICATION_EMERGENCYCALLDATA_CONTROL_XML_TYPE =
                new MediaType("application", "EmergencyCallData.Control+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_DEVICEINFO_XML} media
         * type defined by RFC 7852.
         */
        public static final String APPLICATION_EMERGENCYCALLDATA_DEVICEINFO_XML =
                "application/EmergencyCallData.DeviceInfo+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_DEVICEINFO_XML} media
         * type defined by RFC 7852.
         */
        public static final MediaType APPLICATION_EMERGENCYCALLDATA_DEVICEINFO_XML_TYPE =
                new MediaType("application", "EmergencyCallData.DeviceInfo+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_ECALL_MSD} media
         * type defined by RFC 8147.
         */
        public static final String APPLICATION_EMERGENCYCALLDATA_ECALL_MSD =
                "application/EmergencyCallData.eCall.MSD";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_ECALL_MSD} media
         * type defined by RFC 8147.
         */
        public static final MediaType APPLICATION_EMERGENCYCALLDATA_ECALL_MSD_TYPE =
                new MediaType("application", "EmergencyCallData.eCall.MSD");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_PROVIDERINFO_XML} media
         * type defined by RFC 7852.
         */
        public static final String APPLICATION_EMERGENCYCALLDATA_PROVIDERINFO_XML =
                "application/EmergencyCallData.ProviderInfo+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_PROVIDERINFO_XML} media
         * type defined by RFC 7852.
         */
        public static final MediaType APPLICATION_EMERGENCYCALLDATA_PROVIDERINFO_XML_TYPE =
                new MediaType("application", "EmergencyCallData.ProviderInfo+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_SERVICEINFO_XML} media
         * type defined by RFC 7852.
         */
        public static final String APPLICATION_EMERGENCYCALLDATA_SERVICEINFO_XML =
                "application/EmergencyCallData.ServiceInfo+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_SERVICEINFO_XML} media
         * type defined by RFC 7852.
         */
        public static final MediaType APPLICATION_EMERGENCYCALLDATA_SERVICEINFO_XML_TYPE =
                new MediaType("application", "EmergencyCallData.ServiceInfo+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_SUBSCRIBERINFO_XML} media
         * type defined by RFC 7852.
         */
        public static final String APPLICATION_EMERGENCYCALLDATA_SUBSCRIBERINFO_XML =
                "application/EmergencyCallData.SubscriberInfo+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_SUBSCRIBERINFO_XML} media
         * type defined by RFC 7852.
         */
        public static final MediaType APPLICATION_EMERGENCYCALLDATA_SUBSCRIBERINFO_XML_TYPE =
                new MediaType("application", "EmergencyCallData.SubscriberInfo+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_VEDS_XML} media
         * type defined by RFC 8148, and RFC  Errata 6500.
         */
        public static final String APPLICATION_EMERGENCYCALLDATA_VEDS_XML =
                "application/EmergencyCallData.VEDS+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EMERGENCYCALLDATA_VEDS_XML} media
         * type defined by RFC 8148, and RFC  Errata 6500.
         */
        public static final MediaType APPLICATION_EMERGENCYCALLDATA_VEDS_XML_TYPE =
                new MediaType("application", "EmergencyCallData.VEDS+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EMMA_XML} media
         * type defined by {@code http://www.w3.org/TR/2007/CR-emma-20071211/#media-type-registration}.
         */
        public static final String APPLICATION_EMMA_XML =
                "application/emma+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EMMA_XML} media
         * type defined by {@code http://www.w3.org/TR/2007/CR-emma-20071211/#media-type-registration}.
         */
        public static final MediaType APPLICATION_EMMA_XML_TYPE =
                new MediaType("application", "emma+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ENCAPRTP} media
         * type defined by RFC 6849.
         */
        public static final String APPLICATION_ENCAPRTP =
                "application/encaprtp";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ENCAPRTP} media
         * type defined by RFC 6849.
         */
        public static final MediaType APPLICATION_ENCAPRTP_TYPE =
                new MediaType("application", "encaprtp");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EPP_XML} media
         * type defined by RFC 5730.
         */
        public static final String APPLICATION_EPP_XML =
                "application/epp+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EPP_XML} media
         * type defined by RFC 5730.
         */
        public static final MediaType APPLICATION_EPP_XML_TYPE =
                new MediaType("application", "epp+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final String APPLICATION_EXAMPLE =
                "application/example";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final MediaType APPLICATION_EXAMPLE_TYPE =
                new MediaType("application", "example");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EXI} media
         * type defined by {@code http://www.w3.org/TR/2009/CR-exi-20091208/#mediaTypeRegistration}.
         */
        public static final String APPLICATION_EXI =
                "application/exi";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EXI} media
         * type defined by {@code http://www.w3.org/TR/2009/CR-exi-20091208/#mediaTypeRegistration}.
         */
        public static final MediaType APPLICATION_EXI_TYPE =
                new MediaType("application", "exi");

        /**
         * A {@code String} constant representing {@value #APPLICATION_EXPECT_CT_REPORT_JSON} media
         * type defined by RFC 9163.
         */
        public static final String APPLICATION_EXPECT_CT_REPORT_JSON =
                "application/expect-ct-report+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_EXPECT_CT_REPORT_JSON} media
         * type defined by RFC 9163.
         */
        public static final MediaType APPLICATION_EXPECT_CT_REPORT_JSON_TYPE =
                new MediaType("application", "expect-ct-report+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_FDT_XML} media
         * type defined by RFC 6726.
         */
        public static final String APPLICATION_FDT_XML =
                "application/fdt+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_FDT_XML} media
         * type defined by RFC 6726.
         */
        public static final MediaType APPLICATION_FDT_XML_TYPE =
                new MediaType("application", "fdt+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_FITS} media
         * type defined by RFC 4047.
         */
        public static final String APPLICATION_FITS =
                "application/fits";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_FITS} media
         * type defined by RFC 4047.
         */
        public static final MediaType APPLICATION_FITS_TYPE =
                new MediaType("application", "fits");

        /**
         * A {@code String} constant representing {@value #APPLICATION_FLEXFEC} media
         * type defined by RFC 8627.
         */
        public static final String APPLICATION_FLEXFEC =
                "application/flexfec";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_FLEXFEC} media
         * type defined by RFC 8627.
         */
        public static final MediaType APPLICATION_FLEXFEC_TYPE =
                new MediaType("application", "flexfec");

        /**
         * A {@code String} constant representing {@value #APPLICATION_FONT_SFNT} media
         * type defined by RFC 8081.
         */
        public static final String APPLICATION_FONT_SFNT =
                "application/font-sfnt";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_FONT_SFNT} media
         * type defined by RFC 8081.
         */
        public static final MediaType APPLICATION_FONT_SFNT_TYPE =
                new MediaType("application", "font-sfnt");

        /**
         * A {@code String} constant representing {@value #APPLICATION_FONT_TDPFR} media
         * type defined by RFC 3073.
         */
        public static final String APPLICATION_FONT_TDPFR =
                "application/font-tdpfr";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_FONT_TDPFR} media
         * type defined by RFC 3073.
         */
        public static final MediaType APPLICATION_FONT_TDPFR_TYPE =
                new MediaType("application", "font-tdpfr");

        /**
         * A {@code String} constant representing {@value #APPLICATION_FONT_WOFF} media
         * type defined by RFC 8081.
         */
        public static final String APPLICATION_FONT_WOFF =
                "application/font-woff";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_FONT_WOFF} media
         * type defined by RFC 8081.
         */
        public static final MediaType APPLICATION_FONT_WOFF_TYPE =
                new MediaType("application", "font-woff");

        /**
         * A {@code String} constant representing {@value #APPLICATION_FRAMEWORK_ATTRIBUTES_XML} media
         * type defined by RFC 6230.
         */
        public static final String APPLICATION_FRAMEWORK_ATTRIBUTES_XML =
                "application/framework-attributes+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_FRAMEWORK_ATTRIBUTES_XML} media
         * type defined by RFC 6230.
         */
        public static final MediaType APPLICATION_FRAMEWORK_ATTRIBUTES_XML_TYPE =
                new MediaType("application", "framework-attributes+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_GEO_JSON} media
         * type defined by RFC 7946.
         */
        public static final String APPLICATION_GEO_JSON =
                "application/geo+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_GEO_JSON} media
         * type defined by RFC 7946.
         */
        public static final MediaType APPLICATION_GEO_JSON_TYPE =
                new MediaType("application", "geo+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_GEO_JSON_SEQ} media
         * type defined by RFC 8142.
         */
        public static final String APPLICATION_GEO_JSON_SEQ =
                "application/geo+json-seq";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_GEO_JSON_SEQ} media
         * type defined by RFC 8142.
         */
        public static final MediaType APPLICATION_GEO_JSON_SEQ_TYPE =
                new MediaType("application", "geo+json-seq");

        /**
         * A {@code String} constant representing {@value #APPLICATION_GEOFEED_CSV} media
         * type defined by RFC-ietf-regext-rdap-geofeed-14.
         */
        public static final String APPLICATION_GEOFEED_CSV =
                "application/geofeed+csv";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_GEOFEED_CSV} media
         * type defined by RFC-ietf-regext-rdap-geofeed-14.
         */
        public static final MediaType APPLICATION_GEOFEED_CSV_TYPE =
                new MediaType("application", "geofeed+csv");

        /**
         * A {@code String} constant representing {@value #APPLICATION_GNAP_BINDING_JWS} media
         * type defined by RFC 9635.
         */
        public static final String APPLICATION_GNAP_BINDING_JWS =
                "application/gnap-binding-jws";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_GNAP_BINDING_JWS} media
         * type defined by RFC 9635.
         */
        public static final MediaType APPLICATION_GNAP_BINDING_JWS_TYPE =
                new MediaType("application", "gnap-binding-jws");

        /**
         * A {@code String} constant representing {@value #APPLICATION_GNAP_BINDING_JWSD} media
         * type defined by RFC 9635.
         */
        public static final String APPLICATION_GNAP_BINDING_JWSD =
                "application/gnap-binding-jwsd";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_GNAP_BINDING_JWSD} media
         * type defined by RFC 9635.
         */
        public static final MediaType APPLICATION_GNAP_BINDING_JWSD_TYPE =
                new MediaType("application", "gnap-binding-jwsd");

        /**
         * A {@code String} constant representing {@value #APPLICATION_GNAP_BINDING_ROTATION_JWS} media
         * type defined by RFC 9635.
         */
        public static final String APPLICATION_GNAP_BINDING_ROTATION_JWS =
                "application/gnap-binding-rotation-jws";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_GNAP_BINDING_ROTATION_JWS} media
         * type defined by RFC 9635.
         */
        public static final MediaType APPLICATION_GNAP_BINDING_ROTATION_JWS_TYPE =
                new MediaType("application", "gnap-binding-rotation-jws");

        /**
         * A {@code String} constant representing {@value #APPLICATION_GNAP_BINDING_ROTATION_JWSD} media
         * type defined by RFC 9635.
         */
        public static final String APPLICATION_GNAP_BINDING_ROTATION_JWSD =
                "application/gnap-binding-rotation-jwsd";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_GNAP_BINDING_ROTATION_JWSD} media
         * type defined by RFC 9635.
         */
        public static final MediaType APPLICATION_GNAP_BINDING_ROTATION_JWSD_TYPE =
                new MediaType("application", "gnap-binding-rotation-jwsd");

        /**
         * A {@code String} constant representing {@value #APPLICATION_GZIP} media
         * type defined by RFC 6713.
         */
        public static final String APPLICATION_GZIP =
                "application/gzip";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_GZIP} media
         * type defined by RFC 6713.
         */
        public static final MediaType APPLICATION_GZIP_TYPE =
                new MediaType("application", "gzip");

        /**
         * A {@code String} constant representing {@value #APPLICATION_H224} media
         * type defined by RFC 4573.
         */
        public static final String APPLICATION_H224 =
                "application/H224";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_H224} media
         * type defined by RFC 4573.
         */
        public static final MediaType APPLICATION_H224_TYPE =
                new MediaType("application", "H224");

        /**
         * A {@code String} constant representing {@value #APPLICATION_HELD_XML} media
         * type defined by RFC 5985.
         */
        public static final String APPLICATION_HELD_XML =
                "application/held+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_HELD_XML} media
         * type defined by RFC 5985.
         */
        public static final MediaType APPLICATION_HELD_XML_TYPE =
                new MediaType("application", "held+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_HTTP} media
         * type defined by RFC 9112.
         */
        public static final String APPLICATION_HTTP =
                "application/http";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_HTTP} media
         * type defined by RFC 9112.
         */
        public static final MediaType APPLICATION_HTTP_TYPE =
                new MediaType("application", "http");

        /**
         * A {@code String} constant representing {@value #APPLICATION_IBE_KEY_REQUEST_XML} media
         * type defined by RFC 5408.
         */
        public static final String APPLICATION_IBE_KEY_REQUEST_XML =
                "application/ibe-key-request+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_IBE_KEY_REQUEST_XML} media
         * type defined by RFC 5408.
         */
        public static final MediaType APPLICATION_IBE_KEY_REQUEST_XML_TYPE =
                new MediaType("application", "ibe-key-request+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_IBE_PKG_REPLY_XML} media
         * type defined by RFC 5408.
         */
        public static final String APPLICATION_IBE_PKG_REPLY_XML =
                "application/ibe-pkg-reply+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_IBE_PKG_REPLY_XML} media
         * type defined by RFC 5408.
         */
        public static final MediaType APPLICATION_IBE_PKG_REPLY_XML_TYPE =
                new MediaType("application", "ibe-pkg-reply+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_IBE_PP_DATA} media
         * type defined by RFC 5408.
         */
        public static final String APPLICATION_IBE_PP_DATA =
                "application/ibe-pp-data";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_IBE_PP_DATA} media
         * type defined by RFC 5408.
         */
        public static final MediaType APPLICATION_IBE_PP_DATA_TYPE =
                new MediaType("application", "ibe-pp-data");

        /**
         * A {@code String} constant representing {@value #APPLICATION_IM_ISCOMPOSING_XML} media
         * type defined by RFC 3994.
         */
        public static final String APPLICATION_IM_ISCOMPOSING_XML =
                "application/im-iscomposing+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_IM_ISCOMPOSING_XML} media
         * type defined by RFC 3994.
         */
        public static final MediaType APPLICATION_IM_ISCOMPOSING_XML_TYPE =
                new MediaType("application", "im-iscomposing+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_INDEX} media
         * type defined by RFC 2652.
         */
        public static final String APPLICATION_INDEX =
                "application/index";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_INDEX} media
         * type defined by RFC 2652.
         */
        public static final MediaType APPLICATION_INDEX_TYPE =
                new MediaType("application", "index");

        /**
         * A {@code String} constant representing {@value #APPLICATION_INDEX_CMD} media
         * type defined by RFC 2652.
         */
        public static final String APPLICATION_INDEX_CMD =
                "application/index.cmd";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_INDEX_CMD} media
         * type defined by RFC 2652.
         */
        public static final MediaType APPLICATION_INDEX_CMD_TYPE =
                new MediaType("application", "index.cmd");

        /**
         * A {@code String} constant representing {@value #APPLICATION_INDEX_OBJ} media
         * type defined by RFC 2652.
         */
        public static final String APPLICATION_INDEX_OBJ =
                "application/index.obj";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_INDEX_OBJ} media
         * type defined by RFC 2652.
         */
        public static final MediaType APPLICATION_INDEX_OBJ_TYPE =
                new MediaType("application", "index.obj");

        /**
         * A {@code String} constant representing {@value #APPLICATION_INDEX_RESPONSE} media
         * type defined by RFC 2652.
         */
        public static final String APPLICATION_INDEX_RESPONSE =
                "application/index.response";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_INDEX_RESPONSE} media
         * type defined by RFC 2652.
         */
        public static final MediaType APPLICATION_INDEX_RESPONSE_TYPE =
                new MediaType("application", "index.response");

        /**
         * A {@code String} constant representing {@value #APPLICATION_INDEX_VND} media
         * type defined by RFC 2652.
         */
        public static final String APPLICATION_INDEX_VND =
                "application/index.vnd";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_INDEX_VND} media
         * type defined by RFC 2652.
         */
        public static final MediaType APPLICATION_INDEX_VND_TYPE =
                new MediaType("application", "index.vnd");

        /**
         * A {@code String} constant representing {@value #APPLICATION_IOTP} media
         * type defined by RFC 2935.
         */
        public static final String APPLICATION_IOTP =
                "application/IOTP";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_IOTP} media
         * type defined by RFC 2935.
         */
        public static final MediaType APPLICATION_IOTP_TYPE =
                new MediaType("application", "IOTP");

        /**
         * A {@code String} constant representing {@value #APPLICATION_IPFIX} media
         * type defined by RFC 5655.
         */
        public static final String APPLICATION_IPFIX =
                "application/ipfix";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_IPFIX} media
         * type defined by RFC 5655.
         */
        public static final MediaType APPLICATION_IPFIX_TYPE =
                new MediaType("application", "ipfix");

        /**
         * A {@code String} constant representing {@value #APPLICATION_IPP} media
         * type defined by RFC 8010.
         */
        public static final String APPLICATION_IPP =
                "application/ipp";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_IPP} media
         * type defined by RFC 8010.
         */
        public static final MediaType APPLICATION_IPP_TYPE =
                new MediaType("application", "ipp");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ISUP} media
         * type defined by RFC 3204.
         */
        public static final String APPLICATION_ISUP =
                "application/ISUP";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ISUP} media
         * type defined by RFC 3204.
         */
        public static final MediaType APPLICATION_ISUP_TYPE =
                new MediaType("application", "ISUP");

        /**
         * A {@code String} constant representing {@value #APPLICATION_JAVASCRIPT} media
         * type defined by RFC 4329, and RFC 9239.
         */
        public static final String APPLICATION_JAVASCRIPT =
                "application/javascript";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_JAVASCRIPT} media
         * type defined by RFC 4329, and RFC 9239.
         */
        public static final MediaType APPLICATION_JAVASCRIPT_TYPE =
                new MediaType("application", "javascript");

        /**
         * A {@code String} constant representing {@value #APPLICATION_JOSE} media
         * type defined by RFC 7515.
         */
        public static final String APPLICATION_JOSE =
                "application/jose";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_JOSE} media
         * type defined by RFC 7515.
         */
        public static final MediaType APPLICATION_JOSE_TYPE =
                new MediaType("application", "jose");

        /**
         * A {@code String} constant representing {@value #APPLICATION_JOSE_JSON} media
         * type defined by RFC 7515.
         */
        public static final String APPLICATION_JOSE_JSON =
                "application/jose+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_JOSE_JSON} media
         * type defined by RFC 7515.
         */
        public static final MediaType APPLICATION_JOSE_JSON_TYPE =
                new MediaType("application", "jose+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_JRD_JSON} media
         * type defined by RFC 7033.
         */
        public static final String APPLICATION_JRD_JSON =
                "application/jrd+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_JRD_JSON} media
         * type defined by RFC 7033.
         */
        public static final MediaType APPLICATION_JRD_JSON_TYPE =
                new MediaType("application", "jrd+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_JSCALENDAR_JSON} media
         * type defined by RFC 8984.
         */
        public static final String APPLICATION_JSCALENDAR_JSON =
                "application/jscalendar+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_JSCALENDAR_JSON} media
         * type defined by RFC 8984.
         */
        public static final MediaType APPLICATION_JSCALENDAR_JSON_TYPE =
                new MediaType("application", "jscalendar+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_JSCONTACT_JSON} media
         * type defined by RFC 9553.
         */
        public static final String APPLICATION_JSCONTACT_JSON =
                "application/jscontact+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_JSCONTACT_JSON} media
         * type defined by RFC 9553.
         */
        public static final MediaType APPLICATION_JSCONTACT_JSON_TYPE =
                new MediaType("application", "jscontact+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_JSON} media
         * type defined by RFC 8259.
         */
        public static final String APPLICATION_JSON =
                "application/json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_JSON} media
         * type defined by RFC 8259.
         */
        public static final MediaType APPLICATION_JSON_TYPE =
                new MediaType("application", "json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_JSON_PATCH_JSON} media
         * type defined by RFC 6902.
         */
        public static final String APPLICATION_JSON_PATCH_JSON =
                "application/json-patch+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_JSON_PATCH_JSON} media
         * type defined by RFC 6902.
         */
        public static final MediaType APPLICATION_JSON_PATCH_JSON_TYPE =
                new MediaType("application", "json-patch+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_JSON_SEQ} media
         * type defined by RFC 7464.
         */
        public static final String APPLICATION_JSON_SEQ =
                "application/json-seq";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_JSON_SEQ} media
         * type defined by RFC 7464.
         */
        public static final MediaType APPLICATION_JSON_SEQ_TYPE =
                new MediaType("application", "json-seq");

        /**
         * A {@code String} constant representing {@value #APPLICATION_JSONPATH} media
         * type defined by RFC 9535.
         */
        public static final String APPLICATION_JSONPATH =
                "application/jsonpath";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_JSONPATH} media
         * type defined by RFC 9535.
         */
        public static final MediaType APPLICATION_JSONPATH_TYPE =
                new MediaType("application", "jsonpath");

        /**
         * A {@code String} constant representing {@value #APPLICATION_JWK_JSON} media
         * type defined by RFC 7517.
         */
        public static final String APPLICATION_JWK_JSON =
                "application/jwk+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_JWK_JSON} media
         * type defined by RFC 7517.
         */
        public static final MediaType APPLICATION_JWK_JSON_TYPE =
                new MediaType("application", "jwk+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_JWK_SET_JSON} media
         * type defined by RFC 7517.
         */
        public static final String APPLICATION_JWK_SET_JSON =
                "application/jwk-set+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_JWK_SET_JSON} media
         * type defined by RFC 7517.
         */
        public static final MediaType APPLICATION_JWK_SET_JSON_TYPE =
                new MediaType("application", "jwk-set+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_JWT} media
         * type defined by RFC 7519.
         */
        public static final String APPLICATION_JWT =
                "application/jwt";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_JWT} media
         * type defined by RFC 7519.
         */
        public static final MediaType APPLICATION_JWT_TYPE =
                new MediaType("application", "jwt");

        /**
         * A {@code String} constant representing {@value #APPLICATION_KB_JWT} media
         * type defined by RFC-ietf-oauth-selective-disclosure-jwt-22.
         */
        public static final String APPLICATION_KB_JWT =
                "application/kb+jwt";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_KB_JWT} media
         * type defined by RFC-ietf-oauth-selective-disclosure-jwt-22.
         */
        public static final MediaType APPLICATION_KB_JWT_TYPE =
                new MediaType("application", "kb+jwt");

        /**
         * A {@code String} constant representing {@value #APPLICATION_KPML_REQUEST_XML} media
         * type defined by RFC 4730.
         */
        public static final String APPLICATION_KPML_REQUEST_XML =
                "application/kpml-request+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_KPML_REQUEST_XML} media
         * type defined by RFC 4730.
         */
        public static final MediaType APPLICATION_KPML_REQUEST_XML_TYPE =
                new MediaType("application", "kpml-request+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_KPML_RESPONSE_XML} media
         * type defined by RFC 4730.
         */
        public static final String APPLICATION_KPML_RESPONSE_XML =
                "application/kpml-response+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_KPML_RESPONSE_XML} media
         * type defined by RFC 4730.
         */
        public static final MediaType APPLICATION_KPML_RESPONSE_XML_TYPE =
                new MediaType("application", "kpml-response+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_LGR_XML} media
         * type defined by RFC 7940.
         */
        public static final String APPLICATION_LGR_XML =
                "application/lgr+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_LGR_XML} media
         * type defined by RFC 7940.
         */
        public static final MediaType APPLICATION_LGR_XML_TYPE =
                new MediaType("application", "lgr+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_LINK_FORMAT} media
         * type defined by RFC 6690.
         */
        public static final String APPLICATION_LINK_FORMAT =
                "application/link-format";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_LINK_FORMAT} media
         * type defined by RFC 6690.
         */
        public static final MediaType APPLICATION_LINK_FORMAT_TYPE =
                new MediaType("application", "link-format");

        /**
         * A {@code String} constant representing {@value #APPLICATION_LINKSET} media
         * type defined by RFC 9264.
         */
        public static final String APPLICATION_LINKSET =
                "application/linkset";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_LINKSET} media
         * type defined by RFC 9264.
         */
        public static final MediaType APPLICATION_LINKSET_TYPE =
                new MediaType("application", "linkset");

        /**
         * A {@code String} constant representing {@value #APPLICATION_LINKSET_JSON} media
         * type defined by RFC 9264.
         */
        public static final String APPLICATION_LINKSET_JSON =
                "application/linkset+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_LINKSET_JSON} media
         * type defined by RFC 9264.
         */
        public static final MediaType APPLICATION_LINKSET_JSON_TYPE =
                new MediaType("application", "linkset+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_LOAD_CONTROL_XML} media
         * type defined by RFC 7200.
         */
        public static final String APPLICATION_LOAD_CONTROL_XML =
                "application/load-control+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_LOAD_CONTROL_XML} media
         * type defined by RFC 7200.
         */
        public static final MediaType APPLICATION_LOAD_CONTROL_XML_TYPE =
                new MediaType("application", "load-control+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_LOST_XML} media
         * type defined by RFC 5222.
         */
        public static final String APPLICATION_LOST_XML =
                "application/lost+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_LOST_XML} media
         * type defined by RFC 5222.
         */
        public static final MediaType APPLICATION_LOST_XML_TYPE =
                new MediaType("application", "lost+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_LOSTSYNC_XML} media
         * type defined by RFC 6739.
         */
        public static final String APPLICATION_LOSTSYNC_XML =
                "application/lostsync+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_LOSTSYNC_XML} media
         * type defined by RFC 6739.
         */
        public static final MediaType APPLICATION_LOSTSYNC_XML_TYPE =
                new MediaType("application", "lostsync+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MADS_XML} media
         * type defined by RFC 6207.
         */
        public static final String APPLICATION_MADS_XML =
                "application/mads+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MADS_XML} media
         * type defined by RFC 6207.
         */
        public static final MediaType APPLICATION_MADS_XML_TYPE =
                new MediaType("application", "mads+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MARC} media
         * type defined by RFC 2220.
         */
        public static final String APPLICATION_MARC =
                "application/marc";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MARC} media
         * type defined by RFC 2220.
         */
        public static final MediaType APPLICATION_MARC_TYPE =
                new MediaType("application", "marc");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MARCXML_XML} media
         * type defined by RFC 6207.
         */
        public static final String APPLICATION_MARCXML_XML =
                "application/marcxml+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MARCXML_XML} media
         * type defined by RFC 6207.
         */
        public static final MediaType APPLICATION_MARCXML_XML_TYPE =
                new MediaType("application", "marcxml+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MATHML_XML} media
         * type defined by {@code http://www.w3.org/TR/MathML3/appendixb.html}.
         */
        public static final String APPLICATION_MATHML_XML =
                "application/mathml+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MATHML_XML} media
         * type defined by {@code http://www.w3.org/TR/MathML3/appendixb.html}.
         */
        public static final MediaType APPLICATION_MATHML_XML_TYPE =
                new MediaType("application", "mathml+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MATHML_CONTENT_XML} media
         * type defined by {@code http://www.w3.org/TR/MathML3/appendixb.html}.
         */
        public static final String APPLICATION_MATHML_CONTENT_XML =
                "application/mathml-content+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MATHML_CONTENT_XML} media
         * type defined by {@code http://www.w3.org/TR/MathML3/appendixb.html}.
         */
        public static final MediaType APPLICATION_MATHML_CONTENT_XML_TYPE =
                new MediaType("application", "mathml-content+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MATHML_PRESENTATION_XML} media
         * type defined by {@code http://www.w3.org/TR/MathML3/appendixb.html}.
         */
        public static final String APPLICATION_MATHML_PRESENTATION_XML =
                "application/mathml-presentation+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MATHML_PRESENTATION_XML} media
         * type defined by {@code http://www.w3.org/TR/MathML3/appendixb.html}.
         */
        public static final MediaType APPLICATION_MATHML_PRESENTATION_XML_TYPE =
                new MediaType("application", "mathml-presentation+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MBOX} media
         * type defined by RFC 4155.
         */
        public static final String APPLICATION_MBOX =
                "application/mbox";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MBOX} media
         * type defined by RFC 4155.
         */
        public static final MediaType APPLICATION_MBOX_TYPE =
                new MediaType("application", "mbox");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MEDIA_CONTROL_XML} media
         * type defined by RFC 5168.
         */
        public static final String APPLICATION_MEDIA_CONTROL_XML =
                "application/media_control+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MEDIA_CONTROL_XML} media
         * type defined by RFC 5168.
         */
        public static final MediaType APPLICATION_MEDIA_CONTROL_XML_TYPE =
                new MediaType("application", "media_control+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MEDIA_POLICY_DATASET_XML} media
         * type defined by RFC 6796.
         */
        public static final String APPLICATION_MEDIA_POLICY_DATASET_XML =
                "application/media-policy-dataset+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MEDIA_POLICY_DATASET_XML} media
         * type defined by RFC 6796.
         */
        public static final MediaType APPLICATION_MEDIA_POLICY_DATASET_XML_TYPE =
                new MediaType("application", "media-policy-dataset+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MEDIASERVERCONTROL_XML} media
         * type defined by RFC 5022.
         */
        public static final String APPLICATION_MEDIASERVERCONTROL_XML =
                "application/mediaservercontrol+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MEDIASERVERCONTROL_XML} media
         * type defined by RFC 5022.
         */
        public static final MediaType APPLICATION_MEDIASERVERCONTROL_XML_TYPE =
                new MediaType("application", "mediaservercontrol+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MERGE_PATCH_JSON} media
         * type defined by RFC 7396.
         */
        public static final String APPLICATION_MERGE_PATCH_JSON =
                "application/merge-patch+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MERGE_PATCH_JSON} media
         * type defined by RFC 7396.
         */
        public static final MediaType APPLICATION_MERGE_PATCH_JSON_TYPE =
                new MediaType("application", "merge-patch+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_METALINK4_XML} media
         * type defined by RFC 5854.
         */
        public static final String APPLICATION_METALINK4_XML =
                "application/metalink4+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_METALINK4_XML} media
         * type defined by RFC 5854.
         */
        public static final MediaType APPLICATION_METALINK4_XML_TYPE =
                new MediaType("application", "metalink4+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_METS_XML} media
         * type defined by RFC 6207.
         */
        public static final String APPLICATION_METS_XML =
                "application/mets+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_METS_XML} media
         * type defined by RFC 6207.
         */
        public static final MediaType APPLICATION_METS_XML_TYPE =
                new MediaType("application", "mets+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MIKEY} media
         * type defined by RFC 3830.
         */
        public static final String APPLICATION_MIKEY =
                "application/mikey";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MIKEY} media
         * type defined by RFC 3830.
         */
        public static final MediaType APPLICATION_MIKEY_TYPE =
                new MediaType("application", "mikey");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MISSING_BLOCKS_CBOR_SEQ} media
         * type defined by RFC 9177.
         */
        public static final String APPLICATION_MISSING_BLOCKS_CBOR_SEQ =
                "application/missing-blocks+cbor-seq";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MISSING_BLOCKS_CBOR_SEQ} media
         * type defined by RFC 9177.
         */
        public static final MediaType APPLICATION_MISSING_BLOCKS_CBOR_SEQ_TYPE =
                new MediaType("application", "missing-blocks+cbor-seq");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MODS_XML} media
         * type defined by RFC 6207.
         */
        public static final String APPLICATION_MODS_XML =
                "application/mods+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MODS_XML} media
         * type defined by RFC 6207.
         */
        public static final MediaType APPLICATION_MODS_XML_TYPE =
                new MediaType("application", "mods+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MOSS_KEYS} media
         * type defined by RFC 1848.
         */
        public static final String APPLICATION_MOSS_KEYS =
                "application/moss-keys";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MOSS_KEYS} media
         * type defined by RFC 1848.
         */
        public static final MediaType APPLICATION_MOSS_KEYS_TYPE =
                new MediaType("application", "moss-keys");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MOSS_SIGNATURE} media
         * type defined by RFC 1848.
         */
        public static final String APPLICATION_MOSS_SIGNATURE =
                "application/moss-signature";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MOSS_SIGNATURE} media
         * type defined by RFC 1848.
         */
        public static final MediaType APPLICATION_MOSS_SIGNATURE_TYPE =
                new MediaType("application", "moss-signature");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MOSSKEY_DATA} media
         * type defined by RFC 1848.
         */
        public static final String APPLICATION_MOSSKEY_DATA =
                "application/mosskey-data";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MOSSKEY_DATA} media
         * type defined by RFC 1848.
         */
        public static final MediaType APPLICATION_MOSSKEY_DATA_TYPE =
                new MediaType("application", "mosskey-data");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MOSSKEY_REQUEST} media
         * type defined by RFC 1848.
         */
        public static final String APPLICATION_MOSSKEY_REQUEST =
                "application/mosskey-request";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MOSSKEY_REQUEST} media
         * type defined by RFC 1848.
         */
        public static final MediaType APPLICATION_MOSSKEY_REQUEST_TYPE =
                new MediaType("application", "mosskey-request");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MP21} media
         * type defined by RFC 6381.
         */
        public static final String APPLICATION_MP21 =
                "application/mp21";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MP21} media
         * type defined by RFC 6381.
         */
        public static final MediaType APPLICATION_MP21_TYPE =
                new MediaType("application", "mp21");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MP4} media
         * type defined by RFC 4337, and RFC 6381.
         */
        public static final String APPLICATION_MP4 =
                "application/mp4";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MP4} media
         * type defined by RFC 4337, and RFC 6381.
         */
        public static final MediaType APPLICATION_MP4_TYPE =
                new MediaType("application", "mp4");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MPEG4_GENERIC} media
         * type defined by RFC 3640.
         */
        public static final String APPLICATION_MPEG4_GENERIC =
                "application/mpeg4-generic";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MPEG4_GENERIC} media
         * type defined by RFC 3640.
         */
        public static final MediaType APPLICATION_MPEG4_GENERIC_TYPE =
                new MediaType("application", "mpeg4-generic");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MPEG4_IOD} media
         * type defined by RFC 4337.
         */
        public static final String APPLICATION_MPEG4_IOD =
                "application/mpeg4-iod";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MPEG4_IOD} media
         * type defined by RFC 4337.
         */
        public static final MediaType APPLICATION_MPEG4_IOD_TYPE =
                new MediaType("application", "mpeg4-iod");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MPEG4_IOD_XMT} media
         * type defined by RFC 4337.
         */
        public static final String APPLICATION_MPEG4_IOD_XMT =
                "application/mpeg4-iod-xmt";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MPEG4_IOD_XMT} media
         * type defined by RFC 4337.
         */
        public static final MediaType APPLICATION_MPEG4_IOD_XMT_TYPE =
                new MediaType("application", "mpeg4-iod-xmt");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MRB_CONSUMER_XML} media
         * type defined by RFC 6917.
         */
        public static final String APPLICATION_MRB_CONSUMER_XML =
                "application/mrb-consumer+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MRB_CONSUMER_XML} media
         * type defined by RFC 6917.
         */
        public static final MediaType APPLICATION_MRB_CONSUMER_XML_TYPE =
                new MediaType("application", "mrb-consumer+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MRB_PUBLISH_XML} media
         * type defined by RFC 6917.
         */
        public static final String APPLICATION_MRB_PUBLISH_XML =
                "application/mrb-publish+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MRB_PUBLISH_XML} media
         * type defined by RFC 6917.
         */
        public static final MediaType APPLICATION_MRB_PUBLISH_XML_TYPE =
                new MediaType("application", "mrb-publish+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MSC_IVR_XML} media
         * type defined by RFC 6231.
         */
        public static final String APPLICATION_MSC_IVR_XML =
                "application/msc-ivr+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MSC_IVR_XML} media
         * type defined by RFC 6231.
         */
        public static final MediaType APPLICATION_MSC_IVR_XML_TYPE =
                new MediaType("application", "msc-ivr+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MSC_MIXER_XML} media
         * type defined by RFC 6505.
         */
        public static final String APPLICATION_MSC_MIXER_XML =
                "application/msc-mixer+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MSC_MIXER_XML} media
         * type defined by RFC 6505.
         */
        public static final MediaType APPLICATION_MSC_MIXER_XML_TYPE =
                new MediaType("application", "msc-mixer+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MUD_JSON} media
         * type defined by RFC 8520.
         */
        public static final String APPLICATION_MUD_JSON =
                "application/mud+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MUD_JSON} media
         * type defined by RFC 8520.
         */
        public static final MediaType APPLICATION_MUD_JSON_TYPE =
                new MediaType("application", "mud+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MULTIPART_CORE} media
         * type defined by RFC 8710.
         */
        public static final String APPLICATION_MULTIPART_CORE =
                "application/multipart-core";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MULTIPART_CORE} media
         * type defined by RFC 8710.
         */
        public static final MediaType APPLICATION_MULTIPART_CORE_TYPE =
                new MediaType("application", "multipart-core");

        /**
         * A {@code String} constant representing {@value #APPLICATION_MXF} media
         * type defined by RFC 4539.
         */
        public static final String APPLICATION_MXF =
                "application/mxf";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_MXF} media
         * type defined by RFC 4539.
         */
        public static final MediaType APPLICATION_MXF_TYPE =
                new MediaType("application", "mxf");

        /**
         * A {@code String} constant representing {@value #APPLICATION_NASDATA} media
         * type defined by RFC 4707.
         */
        public static final String APPLICATION_NASDATA =
                "application/nasdata";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_NASDATA} media
         * type defined by RFC 4707.
         */
        public static final MediaType APPLICATION_NASDATA_TYPE =
                new MediaType("application", "nasdata");

        /**
         * A {@code String} constant representing {@value #APPLICATION_NEWS_CHECKGROUPS} media
         * type defined by RFC 5537.
         */
        public static final String APPLICATION_NEWS_CHECKGROUPS =
                "application/news-checkgroups";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_NEWS_CHECKGROUPS} media
         * type defined by RFC 5537.
         */
        public static final MediaType APPLICATION_NEWS_CHECKGROUPS_TYPE =
                new MediaType("application", "news-checkgroups");

        /**
         * A {@code String} constant representing {@value #APPLICATION_NEWS_GROUPINFO} media
         * type defined by RFC 5537.
         */
        public static final String APPLICATION_NEWS_GROUPINFO =
                "application/news-groupinfo";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_NEWS_GROUPINFO} media
         * type defined by RFC 5537.
         */
        public static final MediaType APPLICATION_NEWS_GROUPINFO_TYPE =
                new MediaType("application", "news-groupinfo");

        /**
         * A {@code String} constant representing {@value #APPLICATION_NEWS_TRANSMISSION} media
         * type defined by RFC 5537.
         */
        public static final String APPLICATION_NEWS_TRANSMISSION =
                "application/news-transmission";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_NEWS_TRANSMISSION} media
         * type defined by RFC 5537.
         */
        public static final MediaType APPLICATION_NEWS_TRANSMISSION_TYPE =
                new MediaType("application", "news-transmission");

        /**
         * A {@code String} constant representing {@value #APPLICATION_NLSML_XML} media
         * type defined by RFC 6787.
         */
        public static final String APPLICATION_NLSML_XML =
                "application/nlsml+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_NLSML_XML} media
         * type defined by RFC 6787.
         */
        public static final MediaType APPLICATION_NLSML_XML_TYPE =
                new MediaType("application", "nlsml+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_OAUTH_AUTHZ_REQ_JWT} media
         * type defined by RFC 9101.
         */
        public static final String APPLICATION_OAUTH_AUTHZ_REQ_JWT =
                "application/oauth-authz-req+jwt";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_OAUTH_AUTHZ_REQ_JWT} media
         * type defined by RFC 9101.
         */
        public static final MediaType APPLICATION_OAUTH_AUTHZ_REQ_JWT_TYPE =
                new MediaType("application", "oauth-authz-req+jwt");

        /**
         * A {@code String} constant representing {@value #APPLICATION_OBLIVIOUS_DNS_MESSAGE} media
         * type defined by RFC 9230.
         */
        public static final String APPLICATION_OBLIVIOUS_DNS_MESSAGE =
                "application/oblivious-dns-message";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_OBLIVIOUS_DNS_MESSAGE} media
         * type defined by RFC 9230.
         */
        public static final MediaType APPLICATION_OBLIVIOUS_DNS_MESSAGE_TYPE =
                new MediaType("application", "oblivious-dns-message");

        /**
         * A {@code String} constant representing {@value #APPLICATION_OCSP_REQUEST} media
         * type defined by RFC 6960.
         */
        public static final String APPLICATION_OCSP_REQUEST =
                "application/ocsp-request";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_OCSP_REQUEST} media
         * type defined by RFC 6960.
         */
        public static final MediaType APPLICATION_OCSP_REQUEST_TYPE =
                new MediaType("application", "ocsp-request");

        /**
         * A {@code String} constant representing {@value #APPLICATION_OCSP_RESPONSE} media
         * type defined by RFC 6960.
         */
        public static final String APPLICATION_OCSP_RESPONSE =
                "application/ocsp-response";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_OCSP_RESPONSE} media
         * type defined by RFC 6960.
         */
        public static final MediaType APPLICATION_OCSP_RESPONSE_TYPE =
                new MediaType("application", "ocsp-response");

        /**
         * A {@code String} constant representing {@value #APPLICATION_OCTET_STREAM} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final String APPLICATION_OCTET_STREAM =
                "application/octet-stream";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_OCTET_STREAM} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final MediaType APPLICATION_OCTET_STREAM_TYPE =
                new MediaType("application", "octet-stream");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ODA} media
         * type defined by RFC 1494.
         */
        public static final String APPLICATION_ODA =
                "application/ODA";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ODA} media
         * type defined by RFC 1494.
         */
        public static final MediaType APPLICATION_ODA_TYPE =
                new MediaType("application", "ODA");

        /**
         * A {@code String} constant representing {@value #APPLICATION_OGG} media
         * type defined by RFC 5334, and RFC 7845.
         */
        public static final String APPLICATION_OGG =
                "application/ogg";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_OGG} media
         * type defined by RFC 5334, and RFC 7845.
         */
        public static final MediaType APPLICATION_OGG_TYPE =
                new MediaType("application", "ogg");

        /**
         * A {@code String} constant representing {@value #APPLICATION_OHTTP_KEYS} media
         * type defined by RFC 9458.
         */
        public static final String APPLICATION_OHTTP_KEYS =
                "application/ohttp-keys";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_OHTTP_KEYS} media
         * type defined by RFC 9458.
         */
        public static final MediaType APPLICATION_OHTTP_KEYS_TYPE =
                new MediaType("application", "ohttp-keys");

        /**
         * A {@code String} constant representing {@value #APPLICATION_OSCORE} media
         * type defined by RFC 8613.
         */
        public static final String APPLICATION_OSCORE =
                "application/oscore";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_OSCORE} media
         * type defined by RFC 8613.
         */
        public static final MediaType APPLICATION_OSCORE_TYPE =
                new MediaType("application", "oscore");

        /**
         * A {@code String} constant representing {@value #APPLICATION_P2P_OVERLAY_XML} media
         * type defined by RFC 6940.
         */
        public static final String APPLICATION_P2P_OVERLAY_XML =
                "application/p2p-overlay+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_P2P_OVERLAY_XML} media
         * type defined by RFC 6940.
         */
        public static final MediaType APPLICATION_P2P_OVERLAY_XML_TYPE =
                new MediaType("application", "p2p-overlay+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PARITYFEC} media
         * type defined by RFC 3009.
         */
        public static final String APPLICATION_PARITYFEC =
                "application/parityfec";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PARITYFEC} media
         * type defined by RFC 3009.
         */
        public static final MediaType APPLICATION_PARITYFEC_TYPE =
                new MediaType("application", "parityfec");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PASSPORT} media
         * type defined by RFC 8225.
         */
        public static final String APPLICATION_PASSPORT =
                "application/passport";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PASSPORT} media
         * type defined by RFC 8225.
         */
        public static final MediaType APPLICATION_PASSPORT_TYPE =
                new MediaType("application", "passport");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PATCH_OPS_ERROR_XML} media
         * type defined by RFC 5261.
         */
        public static final String APPLICATION_PATCH_OPS_ERROR_XML =
                "application/patch-ops-error+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PATCH_OPS_ERROR_XML} media
         * type defined by RFC 5261.
         */
        public static final MediaType APPLICATION_PATCH_OPS_ERROR_XML_TYPE =
                new MediaType("application", "patch-ops-error+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PDF} media
         * type defined by RFC 8118.
         */
        public static final String APPLICATION_PDF =
                "application/pdf";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PDF} media
         * type defined by RFC 8118.
         */
        public static final MediaType APPLICATION_PDF_TYPE =
                new MediaType("application", "pdf");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PEM_CERTIFICATE_CHAIN} media
         * type defined by RFC 8555.
         */
        public static final String APPLICATION_PEM_CERTIFICATE_CHAIN =
                "application/pem-certificate-chain";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PEM_CERTIFICATE_CHAIN} media
         * type defined by RFC 8555.
         */
        public static final MediaType APPLICATION_PEM_CERTIFICATE_CHAIN_TYPE =
                new MediaType("application", "pem-certificate-chain");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PGP_ENCRYPTED} media
         * type defined by RFC 3156.
         */
        public static final String APPLICATION_PGP_ENCRYPTED =
                "application/pgp-encrypted";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PGP_ENCRYPTED} media
         * type defined by RFC 3156.
         */
        public static final MediaType APPLICATION_PGP_ENCRYPTED_TYPE =
                new MediaType("application", "pgp-encrypted");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PGP_KEYS} media
         * type defined by RFC 3156.
         */
        public static final String APPLICATION_PGP_KEYS =
                "application/pgp-keys";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PGP_KEYS} media
         * type defined by RFC 3156.
         */
        public static final MediaType APPLICATION_PGP_KEYS_TYPE =
                new MediaType("application", "pgp-keys");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PGP_SIGNATURE} media
         * type defined by RFC 3156.
         */
        public static final String APPLICATION_PGP_SIGNATURE =
                "application/pgp-signature";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PGP_SIGNATURE} media
         * type defined by RFC 3156.
         */
        public static final MediaType APPLICATION_PGP_SIGNATURE_TYPE =
                new MediaType("application", "pgp-signature");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PIDF_DIFF_XML} media
         * type defined by RFC 5262.
         */
        public static final String APPLICATION_PIDF_DIFF_XML =
                "application/pidf-diff+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PIDF_DIFF_XML} media
         * type defined by RFC 5262.
         */
        public static final MediaType APPLICATION_PIDF_DIFF_XML_TYPE =
                new MediaType("application", "pidf-diff+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PIDF_XML} media
         * type defined by RFC 3863.
         */
        public static final String APPLICATION_PIDF_XML =
                "application/pidf+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PIDF_XML} media
         * type defined by RFC 3863.
         */
        public static final MediaType APPLICATION_PIDF_XML_TYPE =
                new MediaType("application", "pidf+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PKCS10} media
         * type defined by RFC 5967.
         */
        public static final String APPLICATION_PKCS10 =
                "application/pkcs10";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PKCS10} media
         * type defined by RFC 5967.
         */
        public static final MediaType APPLICATION_PKCS10_TYPE =
                new MediaType("application", "pkcs10");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PKCS7_MIME} media
         * type defined by RFC 8551, and RFC 7114.
         */
        public static final String APPLICATION_PKCS7_MIME =
                "application/pkcs7-mime";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PKCS7_MIME} media
         * type defined by RFC 8551, and RFC 7114.
         */
        public static final MediaType APPLICATION_PKCS7_MIME_TYPE =
                new MediaType("application", "pkcs7-mime");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PKCS7_SIGNATURE} media
         * type defined by RFC 8551.
         */
        public static final String APPLICATION_PKCS7_SIGNATURE =
                "application/pkcs7-signature";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PKCS7_SIGNATURE} media
         * type defined by RFC 8551.
         */
        public static final MediaType APPLICATION_PKCS7_SIGNATURE_TYPE =
                new MediaType("application", "pkcs7-signature");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PKCS8} media
         * type defined by RFC 5958.
         */
        public static final String APPLICATION_PKCS8 =
                "application/pkcs8";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PKCS8} media
         * type defined by RFC 5958.
         */
        public static final MediaType APPLICATION_PKCS8_TYPE =
                new MediaType("application", "pkcs8");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PKCS8_ENCRYPTED} media
         * type defined by RFC 8351.
         */
        public static final String APPLICATION_PKCS8_ENCRYPTED =
                "application/pkcs8-encrypted";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PKCS8_ENCRYPTED} media
         * type defined by RFC 8351.
         */
        public static final MediaType APPLICATION_PKCS8_ENCRYPTED_TYPE =
                new MediaType("application", "pkcs8-encrypted");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PKIX_ATTR_CERT} media
         * type defined by RFC 5877.
         */
        public static final String APPLICATION_PKIX_ATTR_CERT =
                "application/pkix-attr-cert";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PKIX_ATTR_CERT} media
         * type defined by RFC 5877.
         */
        public static final MediaType APPLICATION_PKIX_ATTR_CERT_TYPE =
                new MediaType("application", "pkix-attr-cert");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PKIX_CERT} media
         * type defined by RFC 2585.
         */
        public static final String APPLICATION_PKIX_CERT =
                "application/pkix-cert";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PKIX_CERT} media
         * type defined by RFC 2585.
         */
        public static final MediaType APPLICATION_PKIX_CERT_TYPE =
                new MediaType("application", "pkix-cert");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PKIX_CRL} media
         * type defined by RFC 2585.
         */
        public static final String APPLICATION_PKIX_CRL =
                "application/pkix-crl";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PKIX_CRL} media
         * type defined by RFC 2585.
         */
        public static final MediaType APPLICATION_PKIX_CRL_TYPE =
                new MediaType("application", "pkix-crl");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PKIX_PKIPATH} media
         * type defined by RFC 6066.
         */
        public static final String APPLICATION_PKIX_PKIPATH =
                "application/pkix-pkipath";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PKIX_PKIPATH} media
         * type defined by RFC 6066.
         */
        public static final MediaType APPLICATION_PKIX_PKIPATH_TYPE =
                new MediaType("application", "pkix-pkipath");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PKIXCMP} media
         * type defined by RFC 9811.
         */
        public static final String APPLICATION_PKIXCMP =
                "application/pkixcmp";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PKIXCMP} media
         * type defined by RFC 9811.
         */
        public static final MediaType APPLICATION_PKIXCMP_TYPE =
                new MediaType("application", "pkixcmp");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PLS_XML} media
         * type defined by RFC 4267.
         */
        public static final String APPLICATION_PLS_XML =
                "application/pls+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PLS_XML} media
         * type defined by RFC 4267.
         */
        public static final MediaType APPLICATION_PLS_XML_TYPE =
                new MediaType("application", "pls+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_POC_SETTINGS_XML} media
         * type defined by RFC 4354.
         */
        public static final String APPLICATION_POC_SETTINGS_XML =
                "application/poc-settings+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_POC_SETTINGS_XML} media
         * type defined by RFC 4354.
         */
        public static final MediaType APPLICATION_POC_SETTINGS_XML_TYPE =
                new MediaType("application", "poc-settings+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_POSTSCRIPT} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final String APPLICATION_POSTSCRIPT =
                "application/postscript";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_POSTSCRIPT} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final MediaType APPLICATION_POSTSCRIPT_TYPE =
                new MediaType("application", "postscript");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PPSP_TRACKER_JSON} media
         * type defined by RFC 7846.
         */
        public static final String APPLICATION_PPSP_TRACKER_JSON =
                "application/ppsp-tracker+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PPSP_TRACKER_JSON} media
         * type defined by RFC 7846.
         */
        public static final MediaType APPLICATION_PPSP_TRACKER_JSON_TYPE =
                new MediaType("application", "ppsp-tracker+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PRIVATE_TOKEN_ISSUER_DIRECTORY} media
         * type defined by RFC 9578.
         */
        public static final String APPLICATION_PRIVATE_TOKEN_ISSUER_DIRECTORY =
                "application/private-token-issuer-directory";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PRIVATE_TOKEN_ISSUER_DIRECTORY} media
         * type defined by RFC 9578.
         */
        public static final MediaType APPLICATION_PRIVATE_TOKEN_ISSUER_DIRECTORY_TYPE =
                new MediaType("application", "private-token-issuer-directory");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PRIVATE_TOKEN_REQUEST} media
         * type defined by RFC 9578.
         */
        public static final String APPLICATION_PRIVATE_TOKEN_REQUEST =
                "application/private-token-request";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PRIVATE_TOKEN_REQUEST} media
         * type defined by RFC 9578.
         */
        public static final MediaType APPLICATION_PRIVATE_TOKEN_REQUEST_TYPE =
                new MediaType("application", "private-token-request");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PRIVATE_TOKEN_RESPONSE} media
         * type defined by RFC 9578.
         */
        public static final String APPLICATION_PRIVATE_TOKEN_RESPONSE =
                "application/private-token-response";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PRIVATE_TOKEN_RESPONSE} media
         * type defined by RFC 9578.
         */
        public static final MediaType APPLICATION_PRIVATE_TOKEN_RESPONSE_TYPE =
                new MediaType("application", "private-token-response");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PROBLEM_JSON} media
         * type defined by RFC 9457.
         */
        public static final String APPLICATION_PROBLEM_JSON =
                "application/problem+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PROBLEM_JSON} media
         * type defined by RFC 9457.
         */
        public static final MediaType APPLICATION_PROBLEM_JSON_TYPE =
                new MediaType("application", "problem+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PROBLEM_XML} media
         * type defined by RFC 9457.
         */
        public static final String APPLICATION_PROBLEM_XML =
                "application/problem+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PROBLEM_XML} media
         * type defined by RFC 9457.
         */
        public static final MediaType APPLICATION_PROBLEM_XML_TYPE =
                new MediaType("application", "problem+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PSKC_XML} media
         * type defined by RFC 6030.
         */
        public static final String APPLICATION_PSKC_XML =
                "application/pskc+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PSKC_XML} media
         * type defined by RFC 6030.
         */
        public static final MediaType APPLICATION_PSKC_XML_TYPE =
                new MediaType("application", "pskc+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_PVD_JSON} media
         * type defined by RFC 8801.
         */
        public static final String APPLICATION_PVD_JSON =
                "application/pvd+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_PVD_JSON} media
         * type defined by RFC 8801.
         */
        public static final MediaType APPLICATION_PVD_JSON_TYPE =
                new MediaType("application", "pvd+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RDF_XML} media
         * type defined by RFC 3870.
         */
        public static final String APPLICATION_RDF_XML =
                "application/rdf+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RDF_XML} media
         * type defined by RFC 3870.
         */
        public static final MediaType APPLICATION_RDF_XML_TYPE =
                new MediaType("application", "rdf+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_QSIG} media
         * type defined by RFC 3204.
         */
        public static final String APPLICATION_QSIG =
                "application/QSIG";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_QSIG} media
         * type defined by RFC 3204.
         */
        public static final MediaType APPLICATION_QSIG_TYPE =
                new MediaType("application", "QSIG");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RAPTORFEC} media
         * type defined by RFC 6682.
         */
        public static final String APPLICATION_RAPTORFEC =
                "application/raptorfec";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RAPTORFEC} media
         * type defined by RFC 6682.
         */
        public static final MediaType APPLICATION_RAPTORFEC_TYPE =
                new MediaType("application", "raptorfec");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RDAP_JSON} media
         * type defined by RFC 9083.
         */
        public static final String APPLICATION_RDAP_JSON =
                "application/rdap+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RDAP_JSON} media
         * type defined by RFC 9083.
         */
        public static final MediaType APPLICATION_RDAP_JSON_TYPE =
                new MediaType("application", "rdap+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_REGINFO_XML} media
         * type defined by RFC 3680.
         */
        public static final String APPLICATION_REGINFO_XML =
                "application/reginfo+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_REGINFO_XML} media
         * type defined by RFC 3680.
         */
        public static final MediaType APPLICATION_REGINFO_XML_TYPE =
                new MediaType("application", "reginfo+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RELAX_NG_COMPACT_SYNTAX} media
         * type defined by {@code http://www.JTC_1sc34.org/repository/0661.pdf}.
         */
        public static final String APPLICATION_RELAX_NG_COMPACT_SYNTAX =
                "application/relax-ng-compact-syntax";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RELAX_NG_COMPACT_SYNTAX} media
         * type defined by {@code http://www.JTC_1sc34.org/repository/0661.pdf}.
         */
        public static final MediaType APPLICATION_RELAX_NG_COMPACT_SYNTAX_TYPE =
                new MediaType("application", "relax-ng-compact-syntax");

        /**
         * A {@code String} constant representing {@value #APPLICATION_REMOTE_PRINTING} media
         * type defined by RFC 1486.
         */
        public static final String APPLICATION_REMOTE_PRINTING =
                "application/remote-printing";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_REMOTE_PRINTING} media
         * type defined by RFC 1486.
         */
        public static final MediaType APPLICATION_REMOTE_PRINTING_TYPE =
                new MediaType("application", "remote-printing");

        /**
         * A {@code String} constant representing {@value #APPLICATION_REPUTON_JSON} media
         * type defined by RFC 7071.
         */
        public static final String APPLICATION_REPUTON_JSON =
                "application/reputon+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_REPUTON_JSON} media
         * type defined by RFC 7071.
         */
        public static final MediaType APPLICATION_REPUTON_JSON_TYPE =
                new MediaType("application", "reputon+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RESOURCE_LISTS_DIFF_XML} media
         * type defined by RFC 5362.
         */
        public static final String APPLICATION_RESOURCE_LISTS_DIFF_XML =
                "application/resource-lists-diff+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RESOURCE_LISTS_DIFF_XML} media
         * type defined by RFC 5362.
         */
        public static final MediaType APPLICATION_RESOURCE_LISTS_DIFF_XML_TYPE =
                new MediaType("application", "resource-lists-diff+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RESOURCE_LISTS_XML} media
         * type defined by RFC 4826.
         */
        public static final String APPLICATION_RESOURCE_LISTS_XML =
                "application/resource-lists+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RESOURCE_LISTS_XML} media
         * type defined by RFC 4826.
         */
        public static final MediaType APPLICATION_RESOURCE_LISTS_XML_TYPE =
                new MediaType("application", "resource-lists+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RFC_XML} media
         * type defined by RFC 7991.
         */
        public static final String APPLICATION_RFC_XML =
                "application/rfc+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RFC_XML} media
         * type defined by RFC 7991.
         */
        public static final MediaType APPLICATION_RFC_XML_TYPE =
                new MediaType("application", "rfc+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RLMI_XML} media
         * type defined by RFC 4662.
         */
        public static final String APPLICATION_RLMI_XML =
                "application/rlmi+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RLMI_XML} media
         * type defined by RFC 4662.
         */
        public static final MediaType APPLICATION_RLMI_XML_TYPE =
                new MediaType("application", "rlmi+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RLS_SERVICES_XML} media
         * type defined by RFC 4826.
         */
        public static final String APPLICATION_RLS_SERVICES_XML =
                "application/rls-services+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RLS_SERVICES_XML} media
         * type defined by RFC 4826.
         */
        public static final MediaType APPLICATION_RLS_SERVICES_XML_TYPE =
                new MediaType("application", "rls-services+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RPKI_CHECKLIST} media
         * type defined by RFC 9323.
         */
        public static final String APPLICATION_RPKI_CHECKLIST =
                "application/rpki-checklist";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RPKI_CHECKLIST} media
         * type defined by RFC 9323.
         */
        public static final MediaType APPLICATION_RPKI_CHECKLIST_TYPE =
                new MediaType("application", "rpki-checklist");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RPKI_GHOSTBUSTERS} media
         * type defined by RFC 6493.
         */
        public static final String APPLICATION_RPKI_GHOSTBUSTERS =
                "application/rpki-ghostbusters";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RPKI_GHOSTBUSTERS} media
         * type defined by RFC 6493.
         */
        public static final MediaType APPLICATION_RPKI_GHOSTBUSTERS_TYPE =
                new MediaType("application", "rpki-ghostbusters");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RPKI_MANIFEST} media
         * type defined by RFC 6481.
         */
        public static final String APPLICATION_RPKI_MANIFEST =
                "application/rpki-manifest";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RPKI_MANIFEST} media
         * type defined by RFC 6481.
         */
        public static final MediaType APPLICATION_RPKI_MANIFEST_TYPE =
                new MediaType("application", "rpki-manifest");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RPKI_PUBLICATION} media
         * type defined by RFC 8181.
         */
        public static final String APPLICATION_RPKI_PUBLICATION =
                "application/rpki-publication";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RPKI_PUBLICATION} media
         * type defined by RFC 8181.
         */
        public static final MediaType APPLICATION_RPKI_PUBLICATION_TYPE =
                new MediaType("application", "rpki-publication");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RPKI_ROA} media
         * type defined by RFC 9582.
         */
        public static final String APPLICATION_RPKI_ROA =
                "application/rpki-roa";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RPKI_ROA} media
         * type defined by RFC 9582.
         */
        public static final MediaType APPLICATION_RPKI_ROA_TYPE =
                new MediaType("application", "rpki-roa");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RPKI_SIGNED_TAL} media
         * type defined by RFC 9691.
         */
        public static final String APPLICATION_RPKI_SIGNED_TAL =
                "application/rpki-signed-tal";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RPKI_SIGNED_TAL} media
         * type defined by RFC 9691.
         */
        public static final MediaType APPLICATION_RPKI_SIGNED_TAL_TYPE =
                new MediaType("application", "rpki-signed-tal");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RPKI_UPDOWN} media
         * type defined by RFC 6492.
         */
        public static final String APPLICATION_RPKI_UPDOWN =
                "application/rpki-updown";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RPKI_UPDOWN} media
         * type defined by RFC 6492.
         */
        public static final MediaType APPLICATION_RPKI_UPDOWN_TYPE =
                new MediaType("application", "rpki-updown");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RS_METADATA_XML} media
         * type defined by RFC 7865, and RFC 9806.
         */
        public static final String APPLICATION_RS_METADATA_XML =
                "application/rs-metadata+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RS_METADATA_XML} media
         * type defined by RFC 7865, and RFC 9806.
         */
        public static final MediaType APPLICATION_RS_METADATA_XML_TYPE =
                new MediaType("application", "rs-metadata+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RTPLOOPBACK} media
         * type defined by RFC 6849.
         */
        public static final String APPLICATION_RTPLOOPBACK =
                "application/rtploopback";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RTPLOOPBACK} media
         * type defined by RFC 6849.
         */
        public static final MediaType APPLICATION_RTPLOOPBACK_TYPE =
                new MediaType("application", "rtploopback");

        /**
         * A {@code String} constant representing {@value #APPLICATION_RTX} media
         * type defined by RFC 4588.
         */
        public static final String APPLICATION_RTX =
                "application/rtx";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_RTX} media
         * type defined by RFC 4588.
         */
        public static final MediaType APPLICATION_RTX_TYPE =
                new MediaType("application", "rtx");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SBML_XML} media
         * type defined by RFC 3823.
         */
        public static final String APPLICATION_SBML_XML =
                "application/sbml+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SBML_XML} media
         * type defined by RFC 3823.
         */
        public static final MediaType APPLICATION_SBML_XML_TYPE =
                new MediaType("application", "sbml+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SCIM_JSON} media
         * type defined by RFC 7644.
         */
        public static final String APPLICATION_SCIM_JSON =
                "application/scim+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SCIM_JSON} media
         * type defined by RFC 7644.
         */
        public static final MediaType APPLICATION_SCIM_JSON_TYPE =
                new MediaType("application", "scim+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SCVP_CV_REQUEST} media
         * type defined by RFC 5055.
         */
        public static final String APPLICATION_SCVP_CV_REQUEST =
                "application/scvp-cv-request";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SCVP_CV_REQUEST} media
         * type defined by RFC 5055.
         */
        public static final MediaType APPLICATION_SCVP_CV_REQUEST_TYPE =
                new MediaType("application", "scvp-cv-request");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SCVP_CV_RESPONSE} media
         * type defined by RFC 5055.
         */
        public static final String APPLICATION_SCVP_CV_RESPONSE =
                "application/scvp-cv-response";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SCVP_CV_RESPONSE} media
         * type defined by RFC 5055.
         */
        public static final MediaType APPLICATION_SCVP_CV_RESPONSE_TYPE =
                new MediaType("application", "scvp-cv-response");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SCVP_VP_REQUEST} media
         * type defined by RFC 5055.
         */
        public static final String APPLICATION_SCVP_VP_REQUEST =
                "application/scvp-vp-request";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SCVP_VP_REQUEST} media
         * type defined by RFC 5055.
         */
        public static final MediaType APPLICATION_SCVP_VP_REQUEST_TYPE =
                new MediaType("application", "scvp-vp-request");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SCVP_VP_RESPONSE} media
         * type defined by RFC 5055.
         */
        public static final String APPLICATION_SCVP_VP_RESPONSE =
                "application/scvp-vp-response";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SCVP_VP_RESPONSE} media
         * type defined by RFC 5055.
         */
        public static final MediaType APPLICATION_SCVP_VP_RESPONSE_TYPE =
                new MediaType("application", "scvp-vp-response");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SD_JWT} media
         * type defined by RFC-ietf-oauth-selective-disclosure-jwt-22.
         */
        public static final String APPLICATION_SD_JWT =
                "application/sd-jwt";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SD_JWT} media
         * type defined by RFC-ietf-oauth-selective-disclosure-jwt-22.
         */
        public static final MediaType APPLICATION_SD_JWT_TYPE =
                new MediaType("application", "sd-jwt");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SD_JWT_JSON} media
         * type defined by RFC-ietf-oauth-selective-disclosure-jwt-22.
         */
        public static final String APPLICATION_SD_JWT_JSON =
                "application/sd-jwt+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SD_JWT_JSON} media
         * type defined by RFC-ietf-oauth-selective-disclosure-jwt-22.
         */
        public static final MediaType APPLICATION_SD_JWT_JSON_TYPE =
                new MediaType("application", "sd-jwt+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SDF_JSON} media
         * type defined by RFC-ietf-asdf-sdf-23.
         */
        public static final String APPLICATION_SDF_JSON =
                "application/sdf+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SDF_JSON} media
         * type defined by RFC-ietf-asdf-sdf-23.
         */
        public static final MediaType APPLICATION_SDF_JSON_TYPE =
                new MediaType("application", "sdf+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SDP} media
         * type defined by RFC 8866.
         */
        public static final String APPLICATION_SDP =
                "application/sdp";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SDP} media
         * type defined by RFC 8866.
         */
        public static final MediaType APPLICATION_SDP_TYPE =
                new MediaType("application", "sdp");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SECEVENT_JWT} media
         * type defined by RFC 8417.
         */
        public static final String APPLICATION_SECEVENT_JWT =
                "application/secevent+jwt";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SECEVENT_JWT} media
         * type defined by RFC 8417.
         */
        public static final MediaType APPLICATION_SECEVENT_JWT_TYPE =
                new MediaType("application", "secevent+jwt");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SENML_ETCH_CBOR} media
         * type defined by RFC 8790.
         */
        public static final String APPLICATION_SENML_ETCH_CBOR =
                "application/senml-etch+cbor";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SENML_ETCH_CBOR} media
         * type defined by RFC 8790.
         */
        public static final MediaType APPLICATION_SENML_ETCH_CBOR_TYPE =
                new MediaType("application", "senml-etch+cbor");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SENML_ETCH_JSON} media
         * type defined by RFC 8790.
         */
        public static final String APPLICATION_SENML_ETCH_JSON =
                "application/senml-etch+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SENML_ETCH_JSON} media
         * type defined by RFC 8790.
         */
        public static final MediaType APPLICATION_SENML_ETCH_JSON_TYPE =
                new MediaType("application", "senml-etch+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SENML_EXI} media
         * type defined by RFC 8428.
         */
        public static final String APPLICATION_SENML_EXI =
                "application/senml-exi";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SENML_EXI} media
         * type defined by RFC 8428.
         */
        public static final MediaType APPLICATION_SENML_EXI_TYPE =
                new MediaType("application", "senml-exi");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SENML_CBOR} media
         * type defined by RFC 8428.
         */
        public static final String APPLICATION_SENML_CBOR =
                "application/senml+cbor";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SENML_CBOR} media
         * type defined by RFC 8428.
         */
        public static final MediaType APPLICATION_SENML_CBOR_TYPE =
                new MediaType("application", "senml+cbor");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SENML_JSON} media
         * type defined by RFC 8428.
         */
        public static final String APPLICATION_SENML_JSON =
                "application/senml+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SENML_JSON} media
         * type defined by RFC 8428.
         */
        public static final MediaType APPLICATION_SENML_JSON_TYPE =
                new MediaType("application", "senml+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SENML_XML} media
         * type defined by RFC 8428.
         */
        public static final String APPLICATION_SENML_XML =
                "application/senml+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SENML_XML} media
         * type defined by RFC 8428.
         */
        public static final MediaType APPLICATION_SENML_XML_TYPE =
                new MediaType("application", "senml+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SENSML_EXI} media
         * type defined by RFC 8428.
         */
        public static final String APPLICATION_SENSML_EXI =
                "application/sensml-exi";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SENSML_EXI} media
         * type defined by RFC 8428.
         */
        public static final MediaType APPLICATION_SENSML_EXI_TYPE =
                new MediaType("application", "sensml-exi");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SENSML_CBOR} media
         * type defined by RFC 8428.
         */
        public static final String APPLICATION_SENSML_CBOR =
                "application/sensml+cbor";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SENSML_CBOR} media
         * type defined by RFC 8428.
         */
        public static final MediaType APPLICATION_SENSML_CBOR_TYPE =
                new MediaType("application", "sensml+cbor");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SENSML_JSON} media
         * type defined by RFC 8428.
         */
        public static final String APPLICATION_SENSML_JSON =
                "application/sensml+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SENSML_JSON} media
         * type defined by RFC 8428.
         */
        public static final MediaType APPLICATION_SENSML_JSON_TYPE =
                new MediaType("application", "sensml+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SENSML_XML} media
         * type defined by RFC 8428.
         */
        public static final String APPLICATION_SENSML_XML =
                "application/sensml+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SENSML_XML} media
         * type defined by RFC 8428.
         */
        public static final MediaType APPLICATION_SENSML_XML_TYPE =
                new MediaType("application", "sensml+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SGML} media
         * type defined by RFC 1874.
         */
        public static final String APPLICATION_SGML =
                "application/SGML";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SGML} media
         * type defined by RFC 1874.
         */
        public static final MediaType APPLICATION_SGML_TYPE =
                new MediaType("application", "SGML");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SHF_XML} media
         * type defined by RFC 4194.
         */
        public static final String APPLICATION_SHF_XML =
                "application/shf+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SHF_XML} media
         * type defined by RFC 4194.
         */
        public static final MediaType APPLICATION_SHF_XML_TYPE =
                new MediaType("application", "shf+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SIEVE} media
         * type defined by RFC 5228.
         */
        public static final String APPLICATION_SIEVE =
                "application/sieve";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SIEVE} media
         * type defined by RFC 5228.
         */
        public static final MediaType APPLICATION_SIEVE_TYPE =
                new MediaType("application", "sieve");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SIMPLE_FILTER_XML} media
         * type defined by RFC 4661.
         */
        public static final String APPLICATION_SIMPLE_FILTER_XML =
                "application/simple-filter+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SIMPLE_FILTER_XML} media
         * type defined by RFC 4661.
         */
        public static final MediaType APPLICATION_SIMPLE_FILTER_XML_TYPE =
                new MediaType("application", "simple-filter+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SIMPLE_MESSAGE_SUMMARY} media
         * type defined by RFC 3842.
         */
        public static final String APPLICATION_SIMPLE_MESSAGE_SUMMARY =
                "application/simple-message-summary";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SIMPLE_MESSAGE_SUMMARY} media
         * type defined by RFC 3842.
         */
        public static final MediaType APPLICATION_SIMPLE_MESSAGE_SUMMARY_TYPE =
                new MediaType("application", "simple-message-summary");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SMIL} media
         * type defined by RFC 4536.
         */
        public static final String APPLICATION_SMIL =
                "application/smil";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SMIL} media
         * type defined by RFC 4536.
         */
        public static final MediaType APPLICATION_SMIL_TYPE =
                new MediaType("application", "smil");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SMIL_XML} media
         * type defined by RFC 4536.
         */
        public static final String APPLICATION_SMIL_XML =
                "application/smil+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SMIL_XML} media
         * type defined by RFC 4536.
         */
        public static final MediaType APPLICATION_SMIL_XML_TYPE =
                new MediaType("application", "smil+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SMPTE336M} media
         * type defined by RFC 6597.
         */
        public static final String APPLICATION_SMPTE336M =
                "application/smpte336m";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SMPTE336M} media
         * type defined by RFC 6597.
         */
        public static final MediaType APPLICATION_SMPTE336M_TYPE =
                new MediaType("application", "smpte336m");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SOAP_XML} media
         * type defined by RFC 3902.
         */
        public static final String APPLICATION_SOAP_XML =
                "application/soap+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SOAP_XML} media
         * type defined by RFC 3902.
         */
        public static final MediaType APPLICATION_SOAP_XML_TYPE =
                new MediaType("application", "soap+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SPARQL_QUERY} media
         * type defined by {@code http://www.w3.org/TR/2007/CR-rdf-sparql-query-20070614/#mediaType}.
         */
        public static final String APPLICATION_SPARQL_QUERY =
                "application/sparql-query";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SPARQL_QUERY} media
         * type defined by {@code http://www.w3.org/TR/2007/CR-rdf-sparql-query-20070614/#mediaType}.
         */
        public static final MediaType APPLICATION_SPARQL_QUERY_TYPE =
                new MediaType("application", "sparql-query");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SPARQL_RESULTS_XML} media
         * type defined by {@code http://www.w3.org/TR/2007/CR-rdf-sparql-XMLres-20070925/#mime}.
         */
        public static final String APPLICATION_SPARQL_RESULTS_XML =
                "application/sparql-results+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SPARQL_RESULTS_XML} media
         * type defined by {@code http://www.w3.org/TR/2007/CR-rdf-sparql-XMLres-20070925/#mime}.
         */
        public static final MediaType APPLICATION_SPARQL_RESULTS_XML_TYPE =
                new MediaType("application", "sparql-results+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SPIRITS_EVENT_XML} media
         * type defined by RFC 3910.
         */
        public static final String APPLICATION_SPIRITS_EVENT_XML =
                "application/spirits-event+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SPIRITS_EVENT_XML} media
         * type defined by RFC 3910.
         */
        public static final MediaType APPLICATION_SPIRITS_EVENT_XML_TYPE =
                new MediaType("application", "spirits-event+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SQL} media
         * type defined by RFC 6922.
         */
        public static final String APPLICATION_SQL =
                "application/sql";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SQL} media
         * type defined by RFC 6922.
         */
        public static final MediaType APPLICATION_SQL_TYPE =
                new MediaType("application", "sql");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SRGS} media
         * type defined by RFC 4267.
         */
        public static final String APPLICATION_SRGS =
                "application/srgs";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SRGS} media
         * type defined by RFC 4267.
         */
        public static final MediaType APPLICATION_SRGS_TYPE =
                new MediaType("application", "srgs");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SRGS_XML} media
         * type defined by RFC 4267.
         */
        public static final String APPLICATION_SRGS_XML =
                "application/srgs+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SRGS_XML} media
         * type defined by RFC 4267.
         */
        public static final MediaType APPLICATION_SRGS_XML_TYPE =
                new MediaType("application", "srgs+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SRU_XML} media
         * type defined by RFC 6207.
         */
        public static final String APPLICATION_SRU_XML =
                "application/sru+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SRU_XML} media
         * type defined by RFC 6207.
         */
        public static final MediaType APPLICATION_SRU_XML_TYPE =
                new MediaType("application", "sru+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SSLKEYLOGFILE} media
         * type defined by RFC-ietf-tls-keylogfile-05.
         */
        public static final String APPLICATION_SSLKEYLOGFILE =
                "application/sslkeylogfile";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SSLKEYLOGFILE} media
         * type defined by RFC-ietf-tls-keylogfile-05.
         */
        public static final MediaType APPLICATION_SSLKEYLOGFILE_TYPE =
                new MediaType("application", "sslkeylogfile");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SSML_XML} media
         * type defined by RFC 4267.
         */
        public static final String APPLICATION_SSML_XML =
                "application/ssml+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SSML_XML} media
         * type defined by RFC 4267.
         */
        public static final MediaType APPLICATION_SSML_XML_TYPE =
                new MediaType("application", "ssml+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SUIT_ENVELOPE_COSE} media
         * type defined by RFC-ietf-suit-manifest-34.
         */
        public static final String APPLICATION_SUIT_ENVELOPE_COSE =
                "application/suit-envelope+cose";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SUIT_ENVELOPE_COSE} media
         * type defined by RFC-ietf-suit-manifest-34.
         */
        public static final MediaType APPLICATION_SUIT_ENVELOPE_COSE_TYPE =
                new MediaType("application", "suit-envelope+cose");

        /**
         * A {@code String} constant representing {@value #APPLICATION_SWID_CBOR} media
         * type defined by RFC 9393.
         */
        public static final String APPLICATION_SWID_CBOR =
                "application/swid+cbor";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_SWID_CBOR} media
         * type defined by RFC 9393.
         */
        public static final MediaType APPLICATION_SWID_CBOR_TYPE =
                new MediaType("application", "swid+cbor");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TAMP_APEX_UPDATE} media
         * type defined by RFC 5934.
         */
        public static final String APPLICATION_TAMP_APEX_UPDATE =
                "application/tamp-apex-update";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TAMP_APEX_UPDATE} media
         * type defined by RFC 5934.
         */
        public static final MediaType APPLICATION_TAMP_APEX_UPDATE_TYPE =
                new MediaType("application", "tamp-apex-update");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TAMP_APEX_UPDATE_CONFIRM} media
         * type defined by RFC 5934.
         */
        public static final String APPLICATION_TAMP_APEX_UPDATE_CONFIRM =
                "application/tamp-apex-update-confirm";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TAMP_APEX_UPDATE_CONFIRM} media
         * type defined by RFC 5934.
         */
        public static final MediaType APPLICATION_TAMP_APEX_UPDATE_CONFIRM_TYPE =
                new MediaType("application", "tamp-apex-update-confirm");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TAMP_COMMUNITY_UPDATE} media
         * type defined by RFC 5934.
         */
        public static final String APPLICATION_TAMP_COMMUNITY_UPDATE =
                "application/tamp-community-update";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TAMP_COMMUNITY_UPDATE} media
         * type defined by RFC 5934.
         */
        public static final MediaType APPLICATION_TAMP_COMMUNITY_UPDATE_TYPE =
                new MediaType("application", "tamp-community-update");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TAMP_COMMUNITY_UPDATE_CONFIRM} media
         * type defined by RFC 5934.
         */
        public static final String APPLICATION_TAMP_COMMUNITY_UPDATE_CONFIRM =
                "application/tamp-community-update-confirm";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TAMP_COMMUNITY_UPDATE_CONFIRM} media
         * type defined by RFC 5934.
         */
        public static final MediaType APPLICATION_TAMP_COMMUNITY_UPDATE_CONFIRM_TYPE =
                new MediaType("application", "tamp-community-update-confirm");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TAMP_ERROR} media
         * type defined by RFC 5934.
         */
        public static final String APPLICATION_TAMP_ERROR =
                "application/tamp-error";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TAMP_ERROR} media
         * type defined by RFC 5934.
         */
        public static final MediaType APPLICATION_TAMP_ERROR_TYPE =
                new MediaType("application", "tamp-error");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TAMP_SEQUENCE_ADJUST} media
         * type defined by RFC 5934.
         */
        public static final String APPLICATION_TAMP_SEQUENCE_ADJUST =
                "application/tamp-sequence-adjust";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TAMP_SEQUENCE_ADJUST} media
         * type defined by RFC 5934.
         */
        public static final MediaType APPLICATION_TAMP_SEQUENCE_ADJUST_TYPE =
                new MediaType("application", "tamp-sequence-adjust");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TAMP_SEQUENCE_ADJUST_CONFIRM} media
         * type defined by RFC 5934.
         */
        public static final String APPLICATION_TAMP_SEQUENCE_ADJUST_CONFIRM =
                "application/tamp-sequence-adjust-confirm";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TAMP_SEQUENCE_ADJUST_CONFIRM} media
         * type defined by RFC 5934.
         */
        public static final MediaType APPLICATION_TAMP_SEQUENCE_ADJUST_CONFIRM_TYPE =
                new MediaType("application", "tamp-sequence-adjust-confirm");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TAMP_STATUS_QUERY} media
         * type defined by RFC 5934.
         */
        public static final String APPLICATION_TAMP_STATUS_QUERY =
                "application/tamp-status-query";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TAMP_STATUS_QUERY} media
         * type defined by RFC 5934.
         */
        public static final MediaType APPLICATION_TAMP_STATUS_QUERY_TYPE =
                new MediaType("application", "tamp-status-query");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TAMP_STATUS_RESPONSE} media
         * type defined by RFC 5934.
         */
        public static final String APPLICATION_TAMP_STATUS_RESPONSE =
                "application/tamp-status-response";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TAMP_STATUS_RESPONSE} media
         * type defined by RFC 5934.
         */
        public static final MediaType APPLICATION_TAMP_STATUS_RESPONSE_TYPE =
                new MediaType("application", "tamp-status-response");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TAMP_UPDATE} media
         * type defined by RFC 5934.
         */
        public static final String APPLICATION_TAMP_UPDATE =
                "application/tamp-update";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TAMP_UPDATE} media
         * type defined by RFC 5934.
         */
        public static final MediaType APPLICATION_TAMP_UPDATE_TYPE =
                new MediaType("application", "tamp-update");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TAMP_UPDATE_CONFIRM} media
         * type defined by RFC 5934.
         */
        public static final String APPLICATION_TAMP_UPDATE_CONFIRM =
                "application/tamp-update-confirm";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TAMP_UPDATE_CONFIRM} media
         * type defined by RFC 5934.
         */
        public static final MediaType APPLICATION_TAMP_UPDATE_CONFIRM_TYPE =
                new MediaType("application", "tamp-update-confirm");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TEI_XML} media
         * type defined by RFC 6129.
         */
        public static final String APPLICATION_TEI_XML =
                "application/tei+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TEI_XML} media
         * type defined by RFC 6129.
         */
        public static final MediaType APPLICATION_TEI_XML_TYPE =
                new MediaType("application", "tei+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_THRAUD_XML} media
         * type defined by RFC 5941.
         */
        public static final String APPLICATION_THRAUD_XML =
                "application/thraud+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_THRAUD_XML} media
         * type defined by RFC 5941.
         */
        public static final MediaType APPLICATION_THRAUD_XML_TYPE =
                new MediaType("application", "thraud+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TIMESTAMP_QUERY} media
         * type defined by RFC 3161.
         */
        public static final String APPLICATION_TIMESTAMP_QUERY =
                "application/timestamp-query";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TIMESTAMP_QUERY} media
         * type defined by RFC 3161.
         */
        public static final MediaType APPLICATION_TIMESTAMP_QUERY_TYPE =
                new MediaType("application", "timestamp-query");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TIMESTAMP_REPLY} media
         * type defined by RFC 3161.
         */
        public static final String APPLICATION_TIMESTAMP_REPLY =
                "application/timestamp-reply";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TIMESTAMP_REPLY} media
         * type defined by RFC 3161.
         */
        public static final MediaType APPLICATION_TIMESTAMP_REPLY_TYPE =
                new MediaType("application", "timestamp-reply");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TIMESTAMPED_DATA} media
         * type defined by RFC 5955.
         */
        public static final String APPLICATION_TIMESTAMPED_DATA =
                "application/timestamped-data";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TIMESTAMPED_DATA} media
         * type defined by RFC 5955.
         */
        public static final MediaType APPLICATION_TIMESTAMPED_DATA_TYPE =
                new MediaType("application", "timestamped-data");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TLSRPT_GZIP} media
         * type defined by RFC 8460.
         */
        public static final String APPLICATION_TLSRPT_GZIP =
                "application/tlsrpt+gzip";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TLSRPT_GZIP} media
         * type defined by RFC 8460.
         */
        public static final MediaType APPLICATION_TLSRPT_GZIP_TYPE =
                new MediaType("application", "tlsrpt+gzip");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TLSRPT_JSON} media
         * type defined by RFC 8460.
         */
        public static final String APPLICATION_TLSRPT_JSON =
                "application/tlsrpt+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TLSRPT_JSON} media
         * type defined by RFC 8460.
         */
        public static final MediaType APPLICATION_TLSRPT_JSON_TYPE =
                new MediaType("application", "tlsrpt+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TNAUTHLIST} media
         * type defined by RFC 8226.
         */
        public static final String APPLICATION_TNAUTHLIST =
                "application/tnauthlist";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TNAUTHLIST} media
         * type defined by RFC 8226.
         */
        public static final MediaType APPLICATION_TNAUTHLIST_TYPE =
                new MediaType("application", "tnauthlist");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TOKEN_INTROSPECTION_JWT} media
         * type defined by RFC 9701.
         */
        public static final String APPLICATION_TOKEN_INTROSPECTION_JWT =
                "application/token-introspection+jwt";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TOKEN_INTROSPECTION_JWT} media
         * type defined by RFC 9701.
         */
        public static final MediaType APPLICATION_TOKEN_INTROSPECTION_JWT_TYPE =
                new MediaType("application", "token-introspection+jwt");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TOML} media
         * type defined by {@code https://github.com/toml-lang/toml/issues/870}.
         */
        public static final String APPLICATION_TOML =
                "application/toml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TOML} media
         * type defined by {@code https://github.com/toml-lang/toml/issues/870}.
         */
        public static final MediaType APPLICATION_TOML_TYPE =
                new MediaType("application", "toml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TRICKLE_ICE_SDPFRAG} media
         * type defined by RFC 8840.
         */
        public static final String APPLICATION_TRICKLE_ICE_SDPFRAG =
                "application/trickle-ice-sdpfrag";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TRICKLE_ICE_SDPFRAG} media
         * type defined by RFC 8840.
         */
        public static final MediaType APPLICATION_TRICKLE_ICE_SDPFRAG_TYPE =
                new MediaType("application", "trickle-ice-sdpfrag");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TZIF} media
         * type defined by RFC 9636.
         */
        public static final String APPLICATION_TZIF =
                "application/tzif";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TZIF} media
         * type defined by RFC 9636.
         */
        public static final MediaType APPLICATION_TZIF_TYPE =
                new MediaType("application", "tzif");

        /**
         * A {@code String} constant representing {@value #APPLICATION_TZIF_LEAP} media
         * type defined by RFC 9636.
         */
        public static final String APPLICATION_TZIF_LEAP =
                "application/tzif-leap";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_TZIF_LEAP} media
         * type defined by RFC 9636.
         */
        public static final MediaType APPLICATION_TZIF_LEAP_TYPE =
                new MediaType("application", "tzif-leap");

        /**
         * A {@code String} constant representing {@value #APPLICATION_UCCS_CBOR} media
         * type defined by RFC 9781.
         */
        public static final String APPLICATION_UCCS_CBOR =
                "application/uccs+cbor";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_UCCS_CBOR} media
         * type defined by RFC 9781.
         */
        public static final MediaType APPLICATION_UCCS_CBOR_TYPE =
                new MediaType("application", "uccs+cbor");

        /**
         * A {@code String} constant representing {@value #APPLICATION_UJCS_JSON} media
         * type defined by RFC 9781.
         */
        public static final String APPLICATION_UJCS_JSON =
                "application/ujcs+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_UJCS_JSON} media
         * type defined by RFC 9781.
         */
        public static final MediaType APPLICATION_UJCS_JSON_TYPE =
                new MediaType("application", "ujcs+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ULPFEC} media
         * type defined by RFC 5109.
         */
        public static final String APPLICATION_ULPFEC =
                "application/ulpfec";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ULPFEC} media
         * type defined by RFC 5109.
         */
        public static final MediaType APPLICATION_ULPFEC_TYPE =
                new MediaType("application", "ulpfec");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VCARD_JSON} media
         * type defined by RFC 7095.
         */
        public static final String APPLICATION_VCARD_JSON =
                "application/vcard+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VCARD_JSON} media
         * type defined by RFC 7095.
         */
        public static final MediaType APPLICATION_VCARD_JSON_TYPE =
                new MediaType("application", "vcard+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VCARD_XML} media
         * type defined by RFC 6351.
         */
        public static final String APPLICATION_VCARD_XML =
                "application/vcard+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VCARD_XML} media
         * type defined by RFC 6351.
         */
        public static final MediaType APPLICATION_VCARD_XML_TYPE =
                new MediaType("application", "vcard+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VEMMI} media
         * type defined by RFC 2122.
         */
        public static final String APPLICATION_VEMMI =
                "application/vemmi";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VEMMI} media
         * type defined by RFC 2122.
         */
        public static final MediaType APPLICATION_VEMMI_TYPE =
                new MediaType("application", "vemmi");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_APPLE_MPEGURL} media
         * type defined by RFC 8216.
         */
        public static final String APPLICATION_VND_APPLE_MPEGURL =
                "application/vnd.apple.mpegurl";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_APPLE_MPEGURL} media
         * type defined by RFC 8216.
         */
        public static final MediaType APPLICATION_VND_APPLE_MPEGURL_TYPE =
                new MediaType("application", "vnd.apple.mpegurl");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_GEO_JSON} media
         * type defined by .
         */
        public static final String APPLICATION_VND_GEO_JSON =
                "application/vnd.geo+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_GEO_JSON} media
         * type defined by .
         */
        public static final MediaType APPLICATION_VND_GEO_JSON_TYPE =
                new MediaType("application", "vnd.geo+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_PWG_MULTIPLEXED} media
         * type defined by RFC 3391.
         */
        public static final String APPLICATION_VND_PWG_MULTIPLEXED =
                "application/vnd.pwg-multiplexed";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_PWG_MULTIPLEXED} media
         * type defined by RFC 3391.
         */
        public static final MediaType APPLICATION_VND_PWG_MULTIPLEXED_TYPE =
                new MediaType("application", "vnd.pwg-multiplexed");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_RADISYS_MOML_XML} media
         * type defined by RFC 5707.
         */
        public static final String APPLICATION_VND_RADISYS_MOML_XML =
                "application/vnd.radisys.moml+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_RADISYS_MOML_XML} media
         * type defined by RFC 5707.
         */
        public static final MediaType APPLICATION_VND_RADISYS_MOML_XML_TYPE =
                new MediaType("application", "vnd.radisys.moml+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_RADISYS_MSML_AUDIT_CONF_XML} media
         * type defined by RFC 5707.
         */
        public static final String APPLICATION_VND_RADISYS_MSML_AUDIT_CONF_XML =
                "application/vnd.radisys.msml-audit-conf+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_RADISYS_MSML_AUDIT_CONF_XML} media
         * type defined by RFC 5707.
         */
        public static final MediaType APPLICATION_VND_RADISYS_MSML_AUDIT_CONF_XML_TYPE =
                new MediaType("application", "vnd.radisys.msml-audit-conf+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_RADISYS_MSML_AUDIT_CONN_XML} media
         * type defined by RFC 5707.
         */
        public static final String APPLICATION_VND_RADISYS_MSML_AUDIT_CONN_XML =
                "application/vnd.radisys.msml-audit-conn+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_RADISYS_MSML_AUDIT_CONN_XML} media
         * type defined by RFC 5707.
         */
        public static final MediaType APPLICATION_VND_RADISYS_MSML_AUDIT_CONN_XML_TYPE =
                new MediaType("application", "vnd.radisys.msml-audit-conn+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_RADISYS_MSML_AUDIT_DIALOG_XML} media
         * type defined by RFC 5707.
         */
        public static final String APPLICATION_VND_RADISYS_MSML_AUDIT_DIALOG_XML =
                "application/vnd.radisys.msml-audit-dialog+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_RADISYS_MSML_AUDIT_DIALOG_XML} media
         * type defined by RFC 5707.
         */
        public static final MediaType APPLICATION_VND_RADISYS_MSML_AUDIT_DIALOG_XML_TYPE =
                new MediaType("application", "vnd.radisys.msml-audit-dialog+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_RADISYS_MSML_AUDIT_STREAM_XML} media
         * type defined by RFC 5707.
         */
        public static final String APPLICATION_VND_RADISYS_MSML_AUDIT_STREAM_XML =
                "application/vnd.radisys.msml-audit-stream+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_RADISYS_MSML_AUDIT_STREAM_XML} media
         * type defined by RFC 5707.
         */
        public static final MediaType APPLICATION_VND_RADISYS_MSML_AUDIT_STREAM_XML_TYPE =
                new MediaType("application", "vnd.radisys.msml-audit-stream+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_RADISYS_MSML_AUDIT_XML} media
         * type defined by RFC 5707.
         */
        public static final String APPLICATION_VND_RADISYS_MSML_AUDIT_XML =
                "application/vnd.radisys.msml-audit+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_RADISYS_MSML_AUDIT_XML} media
         * type defined by RFC 5707.
         */
        public static final MediaType APPLICATION_VND_RADISYS_MSML_AUDIT_XML_TYPE =
                new MediaType("application", "vnd.radisys.msml-audit+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_RADISYS_MSML_CONF_XML} media
         * type defined by RFC 5707.
         */
        public static final String APPLICATION_VND_RADISYS_MSML_CONF_XML =
                "application/vnd.radisys.msml-conf+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_RADISYS_MSML_CONF_XML} media
         * type defined by RFC 5707.
         */
        public static final MediaType APPLICATION_VND_RADISYS_MSML_CONF_XML_TYPE =
                new MediaType("application", "vnd.radisys.msml-conf+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_RADISYS_MSML_DIALOG_BASE_XML} media
         * type defined by RFC 5707.
         */
        public static final String APPLICATION_VND_RADISYS_MSML_DIALOG_BASE_XML =
                "application/vnd.radisys.msml-dialog-base+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_RADISYS_MSML_DIALOG_BASE_XML} media
         * type defined by RFC 5707.
         */
        public static final MediaType APPLICATION_VND_RADISYS_MSML_DIALOG_BASE_XML_TYPE =
                new MediaType("application", "vnd.radisys.msml-dialog-base+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_RADISYS_MSML_DIALOG_FAX_DETECT_XML} media
         * type defined by RFC 5707.
         */
        public static final String APPLICATION_VND_RADISYS_MSML_DIALOG_FAX_DETECT_XML =
                "application/vnd.radisys.msml-dialog-fax-detect+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_RADISYS_MSML_DIALOG_FAX_DETECT_XML} media
         * type defined by RFC 5707.
         */
        public static final MediaType APPLICATION_VND_RADISYS_MSML_DIALOG_FAX_DETECT_XML_TYPE =
                new MediaType("application", "vnd.radisys.msml-dialog-fax-detect+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_RADISYS_MSML_DIALOG_FAX_SENDRECV_XML} media
         * type defined by RFC 5707.
         */
        public static final String APPLICATION_VND_RADISYS_MSML_DIALOG_FAX_SENDRECV_XML =
                "application/vnd.radisys.msml-dialog-fax-sendrecv+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_RADISYS_MSML_DIALOG_FAX_SENDRECV_XML} media
         * type defined by RFC 5707.
         */
        public static final MediaType APPLICATION_VND_RADISYS_MSML_DIALOG_FAX_SENDRECV_XML_TYPE =
                new MediaType("application", "vnd.radisys.msml-dialog-fax-sendrecv+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_RADISYS_MSML_DIALOG_GROUP_XML} media
         * type defined by RFC 5707.
         */
        public static final String APPLICATION_VND_RADISYS_MSML_DIALOG_GROUP_XML =
                "application/vnd.radisys.msml-dialog-group+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_RADISYS_MSML_DIALOG_GROUP_XML} media
         * type defined by RFC 5707.
         */
        public static final MediaType APPLICATION_VND_RADISYS_MSML_DIALOG_GROUP_XML_TYPE =
                new MediaType("application", "vnd.radisys.msml-dialog-group+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_RADISYS_MSML_DIALOG_SPEECH_XML} media
         * type defined by RFC 5707.
         */
        public static final String APPLICATION_VND_RADISYS_MSML_DIALOG_SPEECH_XML =
                "application/vnd.radisys.msml-dialog-speech+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_RADISYS_MSML_DIALOG_SPEECH_XML} media
         * type defined by RFC 5707.
         */
        public static final MediaType APPLICATION_VND_RADISYS_MSML_DIALOG_SPEECH_XML_TYPE =
                new MediaType("application", "vnd.radisys.msml-dialog-speech+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_RADISYS_MSML_DIALOG_TRANSFORM_XML} media
         * type defined by RFC 5707.
         */
        public static final String APPLICATION_VND_RADISYS_MSML_DIALOG_TRANSFORM_XML =
                "application/vnd.radisys.msml-dialog-transform+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_RADISYS_MSML_DIALOG_TRANSFORM_XML} media
         * type defined by RFC 5707.
         */
        public static final MediaType APPLICATION_VND_RADISYS_MSML_DIALOG_TRANSFORM_XML_TYPE =
                new MediaType("application", "vnd.radisys.msml-dialog-transform+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_RADISYS_MSML_DIALOG_XML} media
         * type defined by RFC 5707.
         */
        public static final String APPLICATION_VND_RADISYS_MSML_DIALOG_XML =
                "application/vnd.radisys.msml-dialog+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_RADISYS_MSML_DIALOG_XML} media
         * type defined by RFC 5707.
         */
        public static final MediaType APPLICATION_VND_RADISYS_MSML_DIALOG_XML_TYPE =
                new MediaType("application", "vnd.radisys.msml-dialog+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VND_RADISYS_MSML_XML} media
         * type defined by RFC 5707.
         */
        public static final String APPLICATION_VND_RADISYS_MSML_XML =
                "application/vnd.radisys.msml+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VND_RADISYS_MSML_XML} media
         * type defined by RFC 5707.
         */
        public static final MediaType APPLICATION_VND_RADISYS_MSML_XML_TYPE =
                new MediaType("application", "vnd.radisys.msml+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VOICEXML_XML} media
         * type defined by RFC 4267.
         */
        public static final String APPLICATION_VOICEXML_XML =
                "application/voicexml+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VOICEXML_XML} media
         * type defined by RFC 4267.
         */
        public static final MediaType APPLICATION_VOICEXML_XML_TYPE =
                new MediaType("application", "voicexml+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VOUCHER_CMS_JSON} media
         * type defined by RFC 8366.
         */
        public static final String APPLICATION_VOUCHER_CMS_JSON =
                "application/voucher-cms+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VOUCHER_CMS_JSON} media
         * type defined by RFC 8366.
         */
        public static final MediaType APPLICATION_VOUCHER_CMS_JSON_TYPE =
                new MediaType("application", "voucher-cms+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VOUCHER_JWS_JSON} media
         * type defined by RFC-ietf-anima-jws-voucher-16.
         */
        public static final String APPLICATION_VOUCHER_JWS_JSON =
                "application/voucher-jws+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VOUCHER_JWS_JSON} media
         * type defined by RFC-ietf-anima-jws-voucher-16.
         */
        public static final MediaType APPLICATION_VOUCHER_JWS_JSON_TYPE =
                new MediaType("application", "voucher-jws+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_VQ_RTCPXR} media
         * type defined by RFC 6035.
         */
        public static final String APPLICATION_VQ_RTCPXR =
                "application/vq-rtcpxr";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_VQ_RTCPXR} media
         * type defined by RFC 6035.
         */
        public static final MediaType APPLICATION_VQ_RTCPXR_TYPE =
                new MediaType("application", "vq-rtcpxr");

        /**
         * A {@code String} constant representing {@value #APPLICATION_WATCHERINFO_XML} media
         * type defined by RFC 3858.
         */
        public static final String APPLICATION_WATCHERINFO_XML =
                "application/watcherinfo+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_WATCHERINFO_XML} media
         * type defined by RFC 3858.
         */
        public static final MediaType APPLICATION_WATCHERINFO_XML_TYPE =
                new MediaType("application", "watcherinfo+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_WEBPUSH_OPTIONS_JSON} media
         * type defined by RFC 8292.
         */
        public static final String APPLICATION_WEBPUSH_OPTIONS_JSON =
                "application/webpush-options+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_WEBPUSH_OPTIONS_JSON} media
         * type defined by RFC 8292.
         */
        public static final MediaType APPLICATION_WEBPUSH_OPTIONS_JSON_TYPE =
                new MediaType("application", "webpush-options+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_WHOISPP_QUERY} media
         * type defined by RFC 2957.
         */
        public static final String APPLICATION_WHOISPP_QUERY =
                "application/whoispp-query";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_WHOISPP_QUERY} media
         * type defined by RFC 2957.
         */
        public static final MediaType APPLICATION_WHOISPP_QUERY_TYPE =
                new MediaType("application", "whoispp-query");

        /**
         * A {@code String} constant representing {@value #APPLICATION_WHOISPP_RESPONSE} media
         * type defined by RFC 2958.
         */
        public static final String APPLICATION_WHOISPP_RESPONSE =
                "application/whoispp-response";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_WHOISPP_RESPONSE} media
         * type defined by RFC 2958.
         */
        public static final MediaType APPLICATION_WHOISPP_RESPONSE_TYPE =
                new MediaType("application", "whoispp-response");

        /**
         * A {@code String} constant representing {@value #APPLICATION_X_PKI_MESSAGE} media
         * type defined by RFC 8894.
         */
        public static final String APPLICATION_X_PKI_MESSAGE =
                "application/x-pki-message";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_X_PKI_MESSAGE} media
         * type defined by RFC 8894.
         */
        public static final MediaType APPLICATION_X_PKI_MESSAGE_TYPE =
                new MediaType("application", "x-pki-message");

        /**
         * A {@code String} constant representing {@value #APPLICATION_X_X509_CA_CERT} media
         * type defined by RFC 8894.
         */
        public static final String APPLICATION_X_X509_CA_CERT =
                "application/x-x509-ca-cert";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_X_X509_CA_CERT} media
         * type defined by RFC 8894.
         */
        public static final MediaType APPLICATION_X_X509_CA_CERT_TYPE =
                new MediaType("application", "x-x509-ca-cert");

        /**
         * A {@code String} constant representing {@value #APPLICATION_X_X509_CA_RA_CERT} media
         * type defined by RFC 8894.
         */
        public static final String APPLICATION_X_X509_CA_RA_CERT =
                "application/x-x509-ca-ra-cert";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_X_X509_CA_RA_CERT} media
         * type defined by RFC 8894.
         */
        public static final MediaType APPLICATION_X_X509_CA_RA_CERT_TYPE =
                new MediaType("application", "x-x509-ca-ra-cert");

        /**
         * A {@code String} constant representing {@value #APPLICATION_X_X509_NEXT_CA_CERT} media
         * type defined by RFC 8894.
         */
        public static final String APPLICATION_X_X509_NEXT_CA_CERT =
                "application/x-x509-next-ca-cert";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_X_X509_NEXT_CA_CERT} media
         * type defined by RFC 8894.
         */
        public static final MediaType APPLICATION_X_X509_NEXT_CA_CERT_TYPE =
                new MediaType("application", "x-x509-next-ca-cert");

        /**
         * A {@code String} constant representing {@value #APPLICATION_X400_BP} media
         * type defined by RFC 1494.
         */
        public static final String APPLICATION_X400_BP =
                "application/x400-bp";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_X400_BP} media
         * type defined by RFC 1494.
         */
        public static final MediaType APPLICATION_X400_BP_TYPE =
                new MediaType("application", "x400-bp");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XACML_XML} media
         * type defined by RFC 7061.
         */
        public static final String APPLICATION_XACML_XML =
                "application/xacml+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XACML_XML} media
         * type defined by RFC 7061.
         */
        public static final MediaType APPLICATION_XACML_XML_TYPE =
                new MediaType("application", "xacml+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XCAP_ATT_XML} media
         * type defined by RFC 4825.
         */
        public static final String APPLICATION_XCAP_ATT_XML =
                "application/xcap-att+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XCAP_ATT_XML} media
         * type defined by RFC 4825.
         */
        public static final MediaType APPLICATION_XCAP_ATT_XML_TYPE =
                new MediaType("application", "xcap-att+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XCAP_CAPS_XML} media
         * type defined by RFC 4825.
         */
        public static final String APPLICATION_XCAP_CAPS_XML =
                "application/xcap-caps+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XCAP_CAPS_XML} media
         * type defined by RFC 4825.
         */
        public static final MediaType APPLICATION_XCAP_CAPS_XML_TYPE =
                new MediaType("application", "xcap-caps+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XCAP_DIFF_XML} media
         * type defined by RFC 5874.
         */
        public static final String APPLICATION_XCAP_DIFF_XML =
                "application/xcap-diff+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XCAP_DIFF_XML} media
         * type defined by RFC 5874.
         */
        public static final MediaType APPLICATION_XCAP_DIFF_XML_TYPE =
                new MediaType("application", "xcap-diff+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XCAP_EL_XML} media
         * type defined by RFC 4825.
         */
        public static final String APPLICATION_XCAP_EL_XML =
                "application/xcap-el+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XCAP_EL_XML} media
         * type defined by RFC 4825.
         */
        public static final MediaType APPLICATION_XCAP_EL_XML_TYPE =
                new MediaType("application", "xcap-el+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XCAP_ERROR_XML} media
         * type defined by RFC 4825.
         */
        public static final String APPLICATION_XCAP_ERROR_XML =
                "application/xcap-error+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XCAP_ERROR_XML} media
         * type defined by RFC 4825.
         */
        public static final MediaType APPLICATION_XCAP_ERROR_XML_TYPE =
                new MediaType("application", "xcap-error+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XCAP_NS_XML} media
         * type defined by RFC 4825.
         */
        public static final String APPLICATION_XCAP_NS_XML =
                "application/xcap-ns+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XCAP_NS_XML} media
         * type defined by RFC 4825.
         */
        public static final MediaType APPLICATION_XCAP_NS_XML_TYPE =
                new MediaType("application", "xcap-ns+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XCON_CONFERENCE_INFO_DIFF_XML} media
         * type defined by RFC 6502.
         */
        public static final String APPLICATION_XCON_CONFERENCE_INFO_DIFF_XML =
                "application/xcon-conference-info-diff+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XCON_CONFERENCE_INFO_DIFF_XML} media
         * type defined by RFC 6502.
         */
        public static final MediaType APPLICATION_XCON_CONFERENCE_INFO_DIFF_XML_TYPE =
                new MediaType("application", "xcon-conference-info-diff+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XCON_CONFERENCE_INFO_XML} media
         * type defined by RFC 6502.
         */
        public static final String APPLICATION_XCON_CONFERENCE_INFO_XML =
                "application/xcon-conference-info+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XCON_CONFERENCE_INFO_XML} media
         * type defined by RFC 6502.
         */
        public static final MediaType APPLICATION_XCON_CONFERENCE_INFO_XML_TYPE =
                new MediaType("application", "xcon-conference-info+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XML} media
         * type defined by RFC 7303.
         */
        public static final String APPLICATION_XML =
                "application/xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XML} media
         * type defined by RFC 7303.
         */
        public static final MediaType APPLICATION_XML_TYPE =
                new MediaType("application", "xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XML_DTD} media
         * type defined by RFC 7303.
         */
        public static final String APPLICATION_XML_DTD =
                "application/xml-dtd";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XML_DTD} media
         * type defined by RFC 7303.
         */
        public static final MediaType APPLICATION_XML_DTD_TYPE =
                new MediaType("application", "xml-dtd");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XML_EXTERNAL_PARSED_ENTITY} media
         * type defined by RFC 7303.
         */
        public static final String APPLICATION_XML_EXTERNAL_PARSED_ENTITY =
                "application/xml-external-parsed-entity";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XML_EXTERNAL_PARSED_ENTITY} media
         * type defined by RFC 7303.
         */
        public static final MediaType APPLICATION_XML_EXTERNAL_PARSED_ENTITY_TYPE =
                new MediaType("application", "xml-external-parsed-entity");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XML_PATCH_XML} media
         * type defined by RFC 7351.
         */
        public static final String APPLICATION_XML_PATCH_XML =
                "application/xml-patch+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XML_PATCH_XML} media
         * type defined by RFC 7351.
         */
        public static final MediaType APPLICATION_XML_PATCH_XML_TYPE =
                new MediaType("application", "xml-patch+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XMPP_XML} media
         * type defined by RFC 3923.
         */
        public static final String APPLICATION_XMPP_XML =
                "application/xmpp+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XMPP_XML} media
         * type defined by RFC 3923.
         */
        public static final MediaType APPLICATION_XMPP_XML_TYPE =
                new MediaType("application", "xmpp+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XSLT_XML} media
         * type defined by {@code http://www.w3.org/TR/2007/REC-xslt20-20070123/#media-type-registration}.
         */
        public static final String APPLICATION_XSLT_XML =
                "application/xslt+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XSLT_XML} media
         * type defined by {@code http://www.w3.org/TR/2007/REC-xslt20-20070123/#media-type-registration}.
         */
        public static final MediaType APPLICATION_XSLT_XML_TYPE =
                new MediaType("application", "xslt+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_XV_XML} media
         * type defined by RFC 4374.
         */
        public static final String APPLICATION_XV_XML =
                "application/xv+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_XV_XML} media
         * type defined by RFC 4374.
         */
        public static final MediaType APPLICATION_XV_XML_TYPE =
                new MediaType("application", "xv+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_YAML} media
         * type defined by RFC 9512.
         */
        public static final String APPLICATION_YAML =
                "application/yaml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_YAML} media
         * type defined by RFC 9512.
         */
        public static final MediaType APPLICATION_YAML_TYPE =
                new MediaType("application", "yaml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_YANG} media
         * type defined by RFC 6020.
         */
        public static final String APPLICATION_YANG =
                "application/yang";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_YANG} media
         * type defined by RFC 6020.
         */
        public static final MediaType APPLICATION_YANG_TYPE =
                new MediaType("application", "yang");

        /**
         * A {@code String} constant representing {@value #APPLICATION_YANG_DATA_CBOR} media
         * type defined by RFC 9254.
         */
        public static final String APPLICATION_YANG_DATA_CBOR =
                "application/yang-data+cbor";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_YANG_DATA_CBOR} media
         * type defined by RFC 9254.
         */
        public static final MediaType APPLICATION_YANG_DATA_CBOR_TYPE =
                new MediaType("application", "yang-data+cbor");

        /**
         * A {@code String} constant representing {@value #APPLICATION_YANG_DATA_JSON} media
         * type defined by RFC 8040.
         */
        public static final String APPLICATION_YANG_DATA_JSON =
                "application/yang-data+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_YANG_DATA_JSON} media
         * type defined by RFC 8040.
         */
        public static final MediaType APPLICATION_YANG_DATA_JSON_TYPE =
                new MediaType("application", "yang-data+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_YANG_DATA_XML} media
         * type defined by RFC 8040.
         */
        public static final String APPLICATION_YANG_DATA_XML =
                "application/yang-data+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_YANG_DATA_XML} media
         * type defined by RFC 8040.
         */
        public static final MediaType APPLICATION_YANG_DATA_XML_TYPE =
                new MediaType("application", "yang-data+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_YANG_PATCH_JSON} media
         * type defined by RFC 8072.
         */
        public static final String APPLICATION_YANG_PATCH_JSON =
                "application/yang-patch+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_YANG_PATCH_JSON} media
         * type defined by RFC 8072.
         */
        public static final MediaType APPLICATION_YANG_PATCH_JSON_TYPE =
                new MediaType("application", "yang-patch+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_YANG_PATCH_XML} media
         * type defined by RFC 8072.
         */
        public static final String APPLICATION_YANG_PATCH_XML =
                "application/yang-patch+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_YANG_PATCH_XML} media
         * type defined by RFC 8072.
         */
        public static final MediaType APPLICATION_YANG_PATCH_XML_TYPE =
                new MediaType("application", "yang-patch+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_YANG_SID_JSON} media
         * type defined by RFC 9595.
         */
        public static final String APPLICATION_YANG_SID_JSON =
                "application/yang-sid+json";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_YANG_SID_JSON} media
         * type defined by RFC 9595.
         */
        public static final MediaType APPLICATION_YANG_SID_JSON_TYPE =
                new MediaType("application", "yang-sid+json");

        /**
         * A {@code String} constant representing {@value #APPLICATION_YIN_XML} media
         * type defined by RFC 6020.
         */
        public static final String APPLICATION_YIN_XML =
                "application/yin+xml";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_YIN_XML} media
         * type defined by RFC 6020.
         */
        public static final MediaType APPLICATION_YIN_XML_TYPE =
                new MediaType("application", "yin+xml");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ZLIB} media
         * type defined by RFC 6713.
         */
        public static final String APPLICATION_ZLIB =
                "application/zlib";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ZLIB} media
         * type defined by RFC 6713.
         */
        public static final MediaType APPLICATION_ZLIB_TYPE =
                new MediaType("application", "zlib");

        /**
         * A {@code String} constant representing {@value #APPLICATION_ZSTD} media
         * type defined by RFC 8878.
         */
        public static final String APPLICATION_ZSTD =
                "application/zstd";

        /**
         * A {@link MediaType} constant representing {@value #APPLICATION_ZSTD} media
         * type defined by RFC 8878.
         */
        public static final MediaType APPLICATION_ZSTD_TYPE =
                new MediaType("application", "zstd");
    }

    /**
     * Audio type media subtypes.
     */
    public static class Audio {
        /**
         * A {@code String} constant representing {@value #AUDIO_1D_INTERLEAVED_PARITYFEC} media
         * type defined by RFC 6015.
         */
        public static final String AUDIO_1D_INTERLEAVED_PARITYFEC =
                "audio/1d-interleaved-parityfec";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_1D_INTERLEAVED_PARITYFEC} media
         * type defined by RFC 6015.
         */
        public static final MediaType AUDIO_1D_INTERLEAVED_PARITYFEC_TYPE =
                new MediaType("audio", "1d-interleaved-parityfec");

        /**
         * A {@code String} constant representing {@value #AUDIO_32KADPCM} media
         * type defined by RFC 3802, and RFC 2421.
         */
        public static final String AUDIO_32KADPCM =
                "audio/32kadpcm";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_32KADPCM} media
         * type defined by RFC 3802, and RFC 2421.
         */
        public static final MediaType AUDIO_32KADPCM_TYPE =
                new MediaType("audio", "32kadpcm");

        /**
         * A {@code String} constant representing {@value #AUDIO_3GPP} media
         * type defined by RFC 3839, and RFC 6381.
         */
        public static final String AUDIO_3GPP =
                "audio/3gpp";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_3GPP} media
         * type defined by RFC 3839, and RFC 6381.
         */
        public static final MediaType AUDIO_3GPP_TYPE =
                new MediaType("audio", "3gpp");

        /**
         * A {@code String} constant representing {@value #AUDIO_3GPP2} media
         * type defined by RFC 4393, and RFC 6381.
         */
        public static final String AUDIO_3GPP2 =
                "audio/3gpp2";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_3GPP2} media
         * type defined by RFC 4393, and RFC 6381.
         */
        public static final MediaType AUDIO_3GPP2_TYPE =
                new MediaType("audio", "3gpp2");

        /**
         * A {@code String} constant representing {@value #AUDIO_AC3} media
         * type defined by RFC 4184.
         */
        public static final String AUDIO_AC3 =
                "audio/ac3";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_AC3} media
         * type defined by RFC 4184.
         */
        public static final MediaType AUDIO_AC3_TYPE =
                new MediaType("audio", "ac3");

        /**
         * A {@code String} constant representing {@value #AUDIO_AMR} media
         * type defined by RFC 4867.
         */
        public static final String AUDIO_AMR =
                "audio/AMR";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_AMR} media
         * type defined by RFC 4867.
         */
        public static final MediaType AUDIO_AMR_TYPE =
                new MediaType("audio", "AMR");

        /**
         * A {@code String} constant representing {@value #AUDIO_AMR_WB} media
         * type defined by RFC 4867.
         */
        public static final String AUDIO_AMR_WB =
                "audio/AMR-WB";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_AMR_WB} media
         * type defined by RFC 4867.
         */
        public static final MediaType AUDIO_AMR_WB_TYPE =
                new MediaType("audio", "AMR-WB");

        /**
         * A {@code String} constant representing {@value #AUDIO_AMR_WB_} media
         * type defined by RFC 4352.
         */
        public static final String AUDIO_AMR_WB_ =
                "audio/amr-wb+";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_AMR_WB_} media
         * type defined by RFC 4352.
         */
        public static final MediaType AUDIO_AMR_WB__TYPE =
                new MediaType("audio", "amr-wb+");

        /**
         * A {@code String} constant representing {@value #AUDIO_APTX} media
         * type defined by RFC 7310.
         */
        public static final String AUDIO_APTX =
                "audio/aptx";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_APTX} media
         * type defined by RFC 7310.
         */
        public static final MediaType AUDIO_APTX_TYPE =
                new MediaType("audio", "aptx");

        /**
         * A {@code String} constant representing {@value #AUDIO_ASC} media
         * type defined by RFC 6295.
         */
        public static final String AUDIO_ASC =
                "audio/asc";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_ASC} media
         * type defined by RFC 6295.
         */
        public static final MediaType AUDIO_ASC_TYPE =
                new MediaType("audio", "asc");

        /**
         * A {@code String} constant representing {@value #AUDIO_ATRAC_ADVANCED_LOSSLESS} media
         * type defined by RFC 5584.
         */
        public static final String AUDIO_ATRAC_ADVANCED_LOSSLESS =
                "audio/ATRAC-ADVANCED-LOSSLESS";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_ATRAC_ADVANCED_LOSSLESS} media
         * type defined by RFC 5584.
         */
        public static final MediaType AUDIO_ATRAC_ADVANCED_LOSSLESS_TYPE =
                new MediaType("audio", "ATRAC-ADVANCED-LOSSLESS");

        /**
         * A {@code String} constant representing {@value #AUDIO_ATRAC_X} media
         * type defined by RFC 5584.
         */
        public static final String AUDIO_ATRAC_X =
                "audio/ATRAC-X";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_ATRAC_X} media
         * type defined by RFC 5584.
         */
        public static final MediaType AUDIO_ATRAC_X_TYPE =
                new MediaType("audio", "ATRAC-X");

        /**
         * A {@code String} constant representing {@value #AUDIO_ATRAC3} media
         * type defined by RFC 5584.
         */
        public static final String AUDIO_ATRAC3 =
                "audio/ATRAC3";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_ATRAC3} media
         * type defined by RFC 5584.
         */
        public static final MediaType AUDIO_ATRAC3_TYPE =
                new MediaType("audio", "ATRAC3");

        /**
         * A {@code String} constant representing {@value #AUDIO_BASIC} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final String AUDIO_BASIC =
                "audio/basic";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_BASIC} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final MediaType AUDIO_BASIC_TYPE =
                new MediaType("audio", "basic");

        /**
         * A {@code String} constant representing {@value #AUDIO_BV16} media
         * type defined by RFC 4298.
         */
        public static final String AUDIO_BV16 =
                "audio/BV16";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_BV16} media
         * type defined by RFC 4298.
         */
        public static final MediaType AUDIO_BV16_TYPE =
                new MediaType("audio", "BV16");

        /**
         * A {@code String} constant representing {@value #AUDIO_BV32} media
         * type defined by RFC 4298.
         */
        public static final String AUDIO_BV32 =
                "audio/BV32";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_BV32} media
         * type defined by RFC 4298.
         */
        public static final MediaType AUDIO_BV32_TYPE =
                new MediaType("audio", "BV32");

        /**
         * A {@code String} constant representing {@value #AUDIO_CLEARMODE} media
         * type defined by RFC 4040.
         */
        public static final String AUDIO_CLEARMODE =
                "audio/clearmode";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_CLEARMODE} media
         * type defined by RFC 4040.
         */
        public static final MediaType AUDIO_CLEARMODE_TYPE =
                new MediaType("audio", "clearmode");

        /**
         * A {@code String} constant representing {@value #AUDIO_CN} media
         * type defined by RFC 3389.
         */
        public static final String AUDIO_CN =
                "audio/CN";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_CN} media
         * type defined by RFC 3389.
         */
        public static final MediaType AUDIO_CN_TYPE =
                new MediaType("audio", "CN");

        /**
         * A {@code String} constant representing {@value #AUDIO_DAT12} media
         * type defined by RFC 3190.
         */
        public static final String AUDIO_DAT12 =
                "audio/DAT12";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_DAT12} media
         * type defined by RFC 3190.
         */
        public static final MediaType AUDIO_DAT12_TYPE =
                new MediaType("audio", "DAT12");

        /**
         * A {@code String} constant representing {@value #AUDIO_DLS} media
         * type defined by RFC 4613.
         */
        public static final String AUDIO_DLS =
                "audio/dls";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_DLS} media
         * type defined by RFC 4613.
         */
        public static final MediaType AUDIO_DLS_TYPE =
                new MediaType("audio", "dls");

        /**
         * A {@code String} constant representing {@value #AUDIO_DSR_ES201108} media
         * type defined by RFC 3557.
         */
        public static final String AUDIO_DSR_ES201108 =
                "audio/dsr-es201108";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_DSR_ES201108} media
         * type defined by RFC 3557.
         */
        public static final MediaType AUDIO_DSR_ES201108_TYPE =
                new MediaType("audio", "dsr-es201108");

        /**
         * A {@code String} constant representing {@value #AUDIO_DSR_ES202050} media
         * type defined by RFC 4060.
         */
        public static final String AUDIO_DSR_ES202050 =
                "audio/dsr-es202050";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_DSR_ES202050} media
         * type defined by RFC 4060.
         */
        public static final MediaType AUDIO_DSR_ES202050_TYPE =
                new MediaType("audio", "dsr-es202050");

        /**
         * A {@code String} constant representing {@value #AUDIO_DSR_ES202211} media
         * type defined by RFC 4060.
         */
        public static final String AUDIO_DSR_ES202211 =
                "audio/dsr-es202211";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_DSR_ES202211} media
         * type defined by RFC 4060.
         */
        public static final MediaType AUDIO_DSR_ES202211_TYPE =
                new MediaType("audio", "dsr-es202211");

        /**
         * A {@code String} constant representing {@value #AUDIO_DSR_ES202212} media
         * type defined by RFC 4060.
         */
        public static final String AUDIO_DSR_ES202212 =
                "audio/dsr-es202212";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_DSR_ES202212} media
         * type defined by RFC 4060.
         */
        public static final MediaType AUDIO_DSR_ES202212_TYPE =
                new MediaType("audio", "dsr-es202212");

        /**
         * A {@code String} constant representing {@value #AUDIO_DV} media
         * type defined by RFC 6469.
         */
        public static final String AUDIO_DV =
                "audio/DV";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_DV} media
         * type defined by RFC 6469.
         */
        public static final MediaType AUDIO_DV_TYPE =
                new MediaType("audio", "DV");

        /**
         * A {@code String} constant representing {@value #AUDIO_DVI4} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_DVI4 =
                "audio/DVI4";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_DVI4} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_DVI4_TYPE =
                new MediaType("audio", "DVI4");

        /**
         * A {@code String} constant representing {@value #AUDIO_EAC3} media
         * type defined by RFC 4598.
         */
        public static final String AUDIO_EAC3 =
                "audio/eac3";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_EAC3} media
         * type defined by RFC 4598.
         */
        public static final MediaType AUDIO_EAC3_TYPE =
                new MediaType("audio", "eac3");

        /**
         * A {@code String} constant representing {@value #AUDIO_ENCAPRTP} media
         * type defined by RFC 6849.
         */
        public static final String AUDIO_ENCAPRTP =
                "audio/encaprtp";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_ENCAPRTP} media
         * type defined by RFC 6849.
         */
        public static final MediaType AUDIO_ENCAPRTP_TYPE =
                new MediaType("audio", "encaprtp");

        /**
         * A {@code String} constant representing {@value #AUDIO_EVRC} media
         * type defined by RFC 4788.
         */
        public static final String AUDIO_EVRC =
                "audio/EVRC";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_EVRC} media
         * type defined by RFC 4788.
         */
        public static final MediaType AUDIO_EVRC_TYPE =
                new MediaType("audio", "EVRC");

        /**
         * A {@code String} constant representing {@value #AUDIO_EVRC_QCP} media
         * type defined by RFC 3625.
         */
        public static final String AUDIO_EVRC_QCP =
                "audio/EVRC-QCP";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_EVRC_QCP} media
         * type defined by RFC 3625.
         */
        public static final MediaType AUDIO_EVRC_QCP_TYPE =
                new MediaType("audio", "EVRC-QCP");

        /**
         * A {@code String} constant representing {@value #AUDIO_EVRC0} media
         * type defined by RFC 4788.
         */
        public static final String AUDIO_EVRC0 =
                "audio/EVRC0";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_EVRC0} media
         * type defined by RFC 4788.
         */
        public static final MediaType AUDIO_EVRC0_TYPE =
                new MediaType("audio", "EVRC0");

        /**
         * A {@code String} constant representing {@value #AUDIO_EVRC1} media
         * type defined by RFC 4788.
         */
        public static final String AUDIO_EVRC1 =
                "audio/EVRC1";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_EVRC1} media
         * type defined by RFC 4788.
         */
        public static final MediaType AUDIO_EVRC1_TYPE =
                new MediaType("audio", "EVRC1");

        /**
         * A {@code String} constant representing {@value #AUDIO_EVRCB} media
         * type defined by RFC 5188.
         */
        public static final String AUDIO_EVRCB =
                "audio/EVRCB";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_EVRCB} media
         * type defined by RFC 5188.
         */
        public static final MediaType AUDIO_EVRCB_TYPE =
                new MediaType("audio", "EVRCB");

        /**
         * A {@code String} constant representing {@value #AUDIO_EVRCB0} media
         * type defined by RFC 5188.
         */
        public static final String AUDIO_EVRCB0 =
                "audio/EVRCB0";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_EVRCB0} media
         * type defined by RFC 5188.
         */
        public static final MediaType AUDIO_EVRCB0_TYPE =
                new MediaType("audio", "EVRCB0");

        /**
         * A {@code String} constant representing {@value #AUDIO_EVRCB1} media
         * type defined by RFC 4788.
         */
        public static final String AUDIO_EVRCB1 =
                "audio/EVRCB1";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_EVRCB1} media
         * type defined by RFC 4788.
         */
        public static final MediaType AUDIO_EVRCB1_TYPE =
                new MediaType("audio", "EVRCB1");

        /**
         * A {@code String} constant representing {@value #AUDIO_EVRCNW} media
         * type defined by RFC 6884.
         */
        public static final String AUDIO_EVRCNW =
                "audio/EVRCNW";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_EVRCNW} media
         * type defined by RFC 6884.
         */
        public static final MediaType AUDIO_EVRCNW_TYPE =
                new MediaType("audio", "EVRCNW");

        /**
         * A {@code String} constant representing {@value #AUDIO_EVRCNW0} media
         * type defined by RFC 6884.
         */
        public static final String AUDIO_EVRCNW0 =
                "audio/EVRCNW0";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_EVRCNW0} media
         * type defined by RFC 6884.
         */
        public static final MediaType AUDIO_EVRCNW0_TYPE =
                new MediaType("audio", "EVRCNW0");

        /**
         * A {@code String} constant representing {@value #AUDIO_EVRCNW1} media
         * type defined by RFC 6884.
         */
        public static final String AUDIO_EVRCNW1 =
                "audio/EVRCNW1";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_EVRCNW1} media
         * type defined by RFC 6884.
         */
        public static final MediaType AUDIO_EVRCNW1_TYPE =
                new MediaType("audio", "EVRCNW1");

        /**
         * A {@code String} constant representing {@value #AUDIO_EVRCWB} media
         * type defined by RFC 5188.
         */
        public static final String AUDIO_EVRCWB =
                "audio/EVRCWB";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_EVRCWB} media
         * type defined by RFC 5188.
         */
        public static final MediaType AUDIO_EVRCWB_TYPE =
                new MediaType("audio", "EVRCWB");

        /**
         * A {@code String} constant representing {@value #AUDIO_EVRCWB0} media
         * type defined by RFC 5188.
         */
        public static final String AUDIO_EVRCWB0 =
                "audio/EVRCWB0";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_EVRCWB0} media
         * type defined by RFC 5188.
         */
        public static final MediaType AUDIO_EVRCWB0_TYPE =
                new MediaType("audio", "EVRCWB0");

        /**
         * A {@code String} constant representing {@value #AUDIO_EVRCWB1} media
         * type defined by RFC 5188.
         */
        public static final String AUDIO_EVRCWB1 =
                "audio/EVRCWB1";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_EVRCWB1} media
         * type defined by RFC 5188.
         */
        public static final MediaType AUDIO_EVRCWB1_TYPE =
                new MediaType("audio", "EVRCWB1");

        /**
         * A {@code String} constant representing {@value #AUDIO_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final String AUDIO_EXAMPLE =
                "audio/example";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final MediaType AUDIO_EXAMPLE_TYPE =
                new MediaType("audio", "example");

        /**
         * A {@code String} constant representing {@value #AUDIO_FLAC} media
         * type defined by RFC 9639.
         */
        public static final String AUDIO_FLAC =
                "audio/flac";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_FLAC} media
         * type defined by RFC 9639.
         */
        public static final MediaType AUDIO_FLAC_TYPE =
                new MediaType("audio", "flac");

        /**
         * A {@code String} constant representing {@value #AUDIO_FLEXFEC} media
         * type defined by RFC 8627.
         */
        public static final String AUDIO_FLEXFEC =
                "audio/flexfec";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_FLEXFEC} media
         * type defined by RFC 8627.
         */
        public static final MediaType AUDIO_FLEXFEC_TYPE =
                new MediaType("audio", "flexfec");

        /**
         * A {@code String} constant representing {@value #AUDIO_FWDRED} media
         * type defined by RFC 6354.
         */
        public static final String AUDIO_FWDRED =
                "audio/fwdred";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_FWDRED} media
         * type defined by RFC 6354.
         */
        public static final MediaType AUDIO_FWDRED_TYPE =
                new MediaType("audio", "fwdred");

        /**
         * A {@code String} constant representing {@value #AUDIO_G711_0} media
         * type defined by RFC 7655.
         */
        public static final String AUDIO_G711_0 =
                "audio/G711-0";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_G711_0} media
         * type defined by RFC 7655.
         */
        public static final MediaType AUDIO_G711_0_TYPE =
                new MediaType("audio", "G711-0");

        /**
         * A {@code String} constant representing {@value #AUDIO_G719} media
         * type defined by RFC 5404, and RFC  Errata 3245.
         */
        public static final String AUDIO_G719 =
                "audio/G719";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_G719} media
         * type defined by RFC 5404, and RFC  Errata 3245.
         */
        public static final MediaType AUDIO_G719_TYPE =
                new MediaType("audio", "G719");

        /**
         * A {@code String} constant representing {@value #AUDIO_G7221} media
         * type defined by RFC 5577.
         */
        public static final String AUDIO_G7221 =
                "audio/G7221";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_G7221} media
         * type defined by RFC 5577.
         */
        public static final MediaType AUDIO_G7221_TYPE =
                new MediaType("audio", "G7221");

        /**
         * A {@code String} constant representing {@value #AUDIO_G722} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_G722 =
                "audio/G722";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_G722} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_G722_TYPE =
                new MediaType("audio", "G722");

        /**
         * A {@code String} constant representing {@value #AUDIO_G723} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_G723 =
                "audio/G723";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_G723} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_G723_TYPE =
                new MediaType("audio", "G723");

        /**
         * A {@code String} constant representing {@value #AUDIO_G726_16} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_G726_16 =
                "audio/G726-16";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_G726_16} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_G726_16_TYPE =
                new MediaType("audio", "G726-16");

        /**
         * A {@code String} constant representing {@value #AUDIO_G726_24} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_G726_24 =
                "audio/G726-24";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_G726_24} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_G726_24_TYPE =
                new MediaType("audio", "G726-24");

        /**
         * A {@code String} constant representing {@value #AUDIO_G726_32} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_G726_32 =
                "audio/G726-32";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_G726_32} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_G726_32_TYPE =
                new MediaType("audio", "G726-32");

        /**
         * A {@code String} constant representing {@value #AUDIO_G726_40} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_G726_40 =
                "audio/G726-40";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_G726_40} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_G726_40_TYPE =
                new MediaType("audio", "G726-40");

        /**
         * A {@code String} constant representing {@value #AUDIO_G728} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_G728 =
                "audio/G728";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_G728} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_G728_TYPE =
                new MediaType("audio", "G728");

        /**
         * A {@code String} constant representing {@value #AUDIO_G729} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_G729 =
                "audio/G729";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_G729} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_G729_TYPE =
                new MediaType("audio", "G729");

        /**
         * A {@code String} constant representing {@value #AUDIO_G7291} media
         * type defined by RFC 4749, and RFC 5459.
         */
        public static final String AUDIO_G7291 =
                "audio/G7291";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_G7291} media
         * type defined by RFC 4749, and RFC 5459.
         */
        public static final MediaType AUDIO_G7291_TYPE =
                new MediaType("audio", "G7291");

        /**
         * A {@code String} constant representing {@value #AUDIO_G729D} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_G729D =
                "audio/G729D";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_G729D} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_G729D_TYPE =
                new MediaType("audio", "G729D");

        /**
         * A {@code String} constant representing {@value #AUDIO_G729E} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_G729E =
                "audio/G729E";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_G729E} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_G729E_TYPE =
                new MediaType("audio", "G729E");

        /**
         * A {@code String} constant representing {@value #AUDIO_GSM} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_GSM =
                "audio/GSM";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_GSM} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_GSM_TYPE =
                new MediaType("audio", "GSM");

        /**
         * A {@code String} constant representing {@value #AUDIO_GSM_EFR} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_GSM_EFR =
                "audio/GSM-EFR";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_GSM_EFR} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_GSM_EFR_TYPE =
                new MediaType("audio", "GSM-EFR");

        /**
         * A {@code String} constant representing {@value #AUDIO_GSM_HR_08} media
         * type defined by RFC 5993.
         */
        public static final String AUDIO_GSM_HR_08 =
                "audio/GSM-HR-08";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_GSM_HR_08} media
         * type defined by RFC 5993.
         */
        public static final MediaType AUDIO_GSM_HR_08_TYPE =
                new MediaType("audio", "GSM-HR-08");

        /**
         * A {@code String} constant representing {@value #AUDIO_ILBC} media
         * type defined by RFC 3952.
         */
        public static final String AUDIO_ILBC =
                "audio/iLBC";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_ILBC} media
         * type defined by RFC 3952.
         */
        public static final MediaType AUDIO_ILBC_TYPE =
                new MediaType("audio", "iLBC");

        /**
         * A {@code String} constant representing {@value #AUDIO_IP_MR_V2_5} media
         * type defined by RFC 6262.
         */
        public static final String AUDIO_IP_MR_V2_5 =
                "audio/ip-mr_v2.5";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_IP_MR_V2_5} media
         * type defined by RFC 6262.
         */
        public static final MediaType AUDIO_IP_MR_V2_5_TYPE =
                new MediaType("audio", "ip-mr_v2.5");

        /**
         * A {@code String} constant representing {@value #AUDIO_L8} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_L8 =
                "audio/L8";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_L8} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_L8_TYPE =
                new MediaType("audio", "L8");

        /**
         * A {@code String} constant representing {@value #AUDIO_L16} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_L16 =
                "audio/L16";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_L16} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_L16_TYPE =
                new MediaType("audio", "L16");

        /**
         * A {@code String} constant representing {@value #AUDIO_L20} media
         * type defined by RFC 3190.
         */
        public static final String AUDIO_L20 =
                "audio/L20";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_L20} media
         * type defined by RFC 3190.
         */
        public static final MediaType AUDIO_L20_TYPE =
                new MediaType("audio", "L20");

        /**
         * A {@code String} constant representing {@value #AUDIO_L24} media
         * type defined by RFC 3190.
         */
        public static final String AUDIO_L24 =
                "audio/L24";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_L24} media
         * type defined by RFC 3190.
         */
        public static final MediaType AUDIO_L24_TYPE =
                new MediaType("audio", "L24");

        /**
         * A {@code String} constant representing {@value #AUDIO_LPC} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_LPC =
                "audio/LPC";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_LPC} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_LPC_TYPE =
                new MediaType("audio", "LPC");

        /**
         * A {@code String} constant representing {@value #AUDIO_MATROSKA} media
         * type defined by RFC 9559.
         */
        public static final String AUDIO_MATROSKA =
                "audio/matroska";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_MATROSKA} media
         * type defined by RFC 9559.
         */
        public static final MediaType AUDIO_MATROSKA_TYPE =
                new MediaType("audio", "matroska");

        /**
         * A {@code String} constant representing {@value #AUDIO_MELP} media
         * type defined by RFC 8130.
         */
        public static final String AUDIO_MELP =
                "audio/MELP";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_MELP} media
         * type defined by RFC 8130.
         */
        public static final MediaType AUDIO_MELP_TYPE =
                new MediaType("audio", "MELP");

        /**
         * A {@code String} constant representing {@value #AUDIO_MELP600} media
         * type defined by RFC 8130.
         */
        public static final String AUDIO_MELP600 =
                "audio/MELP600";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_MELP600} media
         * type defined by RFC 8130.
         */
        public static final MediaType AUDIO_MELP600_TYPE =
                new MediaType("audio", "MELP600");

        /**
         * A {@code String} constant representing {@value #AUDIO_MELP1200} media
         * type defined by RFC 8130.
         */
        public static final String AUDIO_MELP1200 =
                "audio/MELP1200";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_MELP1200} media
         * type defined by RFC 8130.
         */
        public static final MediaType AUDIO_MELP1200_TYPE =
                new MediaType("audio", "MELP1200");

        /**
         * A {@code String} constant representing {@value #AUDIO_MELP2400} media
         * type defined by RFC 8130.
         */
        public static final String AUDIO_MELP2400 =
                "audio/MELP2400";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_MELP2400} media
         * type defined by RFC 8130.
         */
        public static final MediaType AUDIO_MELP2400_TYPE =
                new MediaType("audio", "MELP2400");

        /**
         * A {@code String} constant representing {@value #AUDIO_MOBILE_XMF} media
         * type defined by RFC 4723.
         */
        public static final String AUDIO_MOBILE_XMF =
                "audio/mobile-xmf";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_MOBILE_XMF} media
         * type defined by RFC 4723.
         */
        public static final MediaType AUDIO_MOBILE_XMF_TYPE =
                new MediaType("audio", "mobile-xmf");

        /**
         * A {@code String} constant representing {@value #AUDIO_MPA} media
         * type defined by RFC 3555.
         */
        public static final String AUDIO_MPA =
                "audio/MPA";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_MPA} media
         * type defined by RFC 3555.
         */
        public static final MediaType AUDIO_MPA_TYPE =
                new MediaType("audio", "MPA");

        /**
         * A {@code String} constant representing {@value #AUDIO_MP4} media
         * type defined by RFC 4337, and RFC 6381.
         */
        public static final String AUDIO_MP4 =
                "audio/mp4";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_MP4} media
         * type defined by RFC 4337, and RFC 6381.
         */
        public static final MediaType AUDIO_MP4_TYPE =
                new MediaType("audio", "mp4");

        /**
         * A {@code String} constant representing {@value #AUDIO_MP4A_LATM} media
         * type defined by RFC 6416.
         */
        public static final String AUDIO_MP4A_LATM =
                "audio/MP4A-LATM";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_MP4A_LATM} media
         * type defined by RFC 6416.
         */
        public static final MediaType AUDIO_MP4A_LATM_TYPE =
                new MediaType("audio", "MP4A-LATM");

        /**
         * A {@code String} constant representing {@value #AUDIO_MPA_ROBUST} media
         * type defined by RFC 5219.
         */
        public static final String AUDIO_MPA_ROBUST =
                "audio/mpa-robust";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_MPA_ROBUST} media
         * type defined by RFC 5219.
         */
        public static final MediaType AUDIO_MPA_ROBUST_TYPE =
                new MediaType("audio", "mpa-robust");

        /**
         * A {@code String} constant representing {@value #AUDIO_MPEG} media
         * type defined by RFC 3003.
         */
        public static final String AUDIO_MPEG =
                "audio/mpeg";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_MPEG} media
         * type defined by RFC 3003.
         */
        public static final MediaType AUDIO_MPEG_TYPE =
                new MediaType("audio", "mpeg");

        /**
         * A {@code String} constant representing {@value #AUDIO_MPEG4_GENERIC} media
         * type defined by RFC 3640, and RFC 5691, and RFC 6295.
         */
        public static final String AUDIO_MPEG4_GENERIC =
                "audio/mpeg4-generic";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_MPEG4_GENERIC} media
         * type defined by RFC 3640, and RFC 5691, and RFC 6295.
         */
        public static final MediaType AUDIO_MPEG4_GENERIC_TYPE =
                new MediaType("audio", "mpeg4-generic");

        /**
         * A {@code String} constant representing {@value #AUDIO_OGG} media
         * type defined by RFC 5334, and RFC 7845.
         */
        public static final String AUDIO_OGG =
                "audio/ogg";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_OGG} media
         * type defined by RFC 5334, and RFC 7845.
         */
        public static final MediaType AUDIO_OGG_TYPE =
                new MediaType("audio", "ogg");

        /**
         * A {@code String} constant representing {@value #AUDIO_OPUS} media
         * type defined by RFC 7587.
         */
        public static final String AUDIO_OPUS =
                "audio/opus";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_OPUS} media
         * type defined by RFC 7587.
         */
        public static final MediaType AUDIO_OPUS_TYPE =
                new MediaType("audio", "opus");

        /**
         * A {@code String} constant representing {@value #AUDIO_PARITYFEC} media
         * type defined by RFC 3009.
         */
        public static final String AUDIO_PARITYFEC =
                "audio/parityfec";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_PARITYFEC} media
         * type defined by RFC 3009.
         */
        public static final MediaType AUDIO_PARITYFEC_TYPE =
                new MediaType("audio", "parityfec");

        /**
         * A {@code String} constant representing {@value #AUDIO_PCMA} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_PCMA =
                "audio/PCMA";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_PCMA} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_PCMA_TYPE =
                new MediaType("audio", "PCMA");

        /**
         * A {@code String} constant representing {@value #AUDIO_PCMA_WB} media
         * type defined by RFC 5391.
         */
        public static final String AUDIO_PCMA_WB =
                "audio/PCMA-WB";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_PCMA_WB} media
         * type defined by RFC 5391.
         */
        public static final MediaType AUDIO_PCMA_WB_TYPE =
                new MediaType("audio", "PCMA-WB");

        /**
         * A {@code String} constant representing {@value #AUDIO_PCMU} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_PCMU =
                "audio/PCMU";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_PCMU} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_PCMU_TYPE =
                new MediaType("audio", "PCMU");

        /**
         * A {@code String} constant representing {@value #AUDIO_PCMU_WB} media
         * type defined by RFC 5391.
         */
        public static final String AUDIO_PCMU_WB =
                "audio/PCMU-WB";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_PCMU_WB} media
         * type defined by RFC 5391.
         */
        public static final MediaType AUDIO_PCMU_WB_TYPE =
                new MediaType("audio", "PCMU-WB");

        /**
         * A {@code String} constant representing {@value #AUDIO_QCELP} media
         * type defined by RFC 3555, and RFC 3625.
         */
        public static final String AUDIO_QCELP =
                "audio/QCELP";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_QCELP} media
         * type defined by RFC 3555, and RFC 3625.
         */
        public static final MediaType AUDIO_QCELP_TYPE =
                new MediaType("audio", "QCELP");

        /**
         * A {@code String} constant representing {@value #AUDIO_RAPTORFEC} media
         * type defined by RFC 6682.
         */
        public static final String AUDIO_RAPTORFEC =
                "audio/raptorfec";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_RAPTORFEC} media
         * type defined by RFC 6682.
         */
        public static final MediaType AUDIO_RAPTORFEC_TYPE =
                new MediaType("audio", "raptorfec");

        /**
         * A {@code String} constant representing {@value #AUDIO_RED} media
         * type defined by RFC 3555.
         */
        public static final String AUDIO_RED =
                "audio/RED";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_RED} media
         * type defined by RFC 3555.
         */
        public static final MediaType AUDIO_RED_TYPE =
                new MediaType("audio", "RED");

        /**
         * A {@code String} constant representing {@value #AUDIO_RTPLOOPBACK} media
         * type defined by RFC 6849.
         */
        public static final String AUDIO_RTPLOOPBACK =
                "audio/rtploopback";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_RTPLOOPBACK} media
         * type defined by RFC 6849.
         */
        public static final MediaType AUDIO_RTPLOOPBACK_TYPE =
                new MediaType("audio", "rtploopback");

        /**
         * A {@code String} constant representing {@value #AUDIO_RTP_MIDI} media
         * type defined by RFC 6295.
         */
        public static final String AUDIO_RTP_MIDI =
                "audio/rtp-midi";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_RTP_MIDI} media
         * type defined by RFC 6295.
         */
        public static final MediaType AUDIO_RTP_MIDI_TYPE =
                new MediaType("audio", "rtp-midi");

        /**
         * A {@code String} constant representing {@value #AUDIO_RTX} media
         * type defined by RFC 4588.
         */
        public static final String AUDIO_RTX =
                "audio/rtx";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_RTX} media
         * type defined by RFC 4588.
         */
        public static final MediaType AUDIO_RTX_TYPE =
                new MediaType("audio", "rtx");

        /**
         * A {@code String} constant representing {@value #AUDIO_SCIP} media
         * type defined by RFC 9607.
         */
        public static final String AUDIO_SCIP =
                "audio/scip";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_SCIP} media
         * type defined by RFC 9607.
         */
        public static final MediaType AUDIO_SCIP_TYPE =
                new MediaType("audio", "scip");

        /**
         * A {@code String} constant representing {@value #AUDIO_SMV} media
         * type defined by RFC 3558.
         */
        public static final String AUDIO_SMV =
                "audio/SMV";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_SMV} media
         * type defined by RFC 3558.
         */
        public static final MediaType AUDIO_SMV_TYPE =
                new MediaType("audio", "SMV");

        /**
         * A {@code String} constant representing {@value #AUDIO_SMV0} media
         * type defined by RFC 3558.
         */
        public static final String AUDIO_SMV0 =
                "audio/SMV0";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_SMV0} media
         * type defined by RFC 3558.
         */
        public static final MediaType AUDIO_SMV0_TYPE =
                new MediaType("audio", "SMV0");

        /**
         * A {@code String} constant representing {@value #AUDIO_SMV_QCP} media
         * type defined by RFC 3625.
         */
        public static final String AUDIO_SMV_QCP =
                "audio/SMV-QCP";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_SMV_QCP} media
         * type defined by RFC 3625.
         */
        public static final MediaType AUDIO_SMV_QCP_TYPE =
                new MediaType("audio", "SMV-QCP");

        /**
         * A {@code String} constant representing {@value #AUDIO_SPEEX} media
         * type defined by RFC 5574.
         */
        public static final String AUDIO_SPEEX =
                "audio/speex";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_SPEEX} media
         * type defined by RFC 5574.
         */
        public static final MediaType AUDIO_SPEEX_TYPE =
                new MediaType("audio", "speex");

        /**
         * A {@code String} constant representing {@value #AUDIO_T140C} media
         * type defined by RFC 4351.
         */
        public static final String AUDIO_T140C =
                "audio/t140c";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_T140C} media
         * type defined by RFC 4351.
         */
        public static final MediaType AUDIO_T140C_TYPE =
                new MediaType("audio", "t140c");

        /**
         * A {@code String} constant representing {@value #AUDIO_T38} media
         * type defined by RFC 4612.
         */
        public static final String AUDIO_T38 =
                "audio/t38";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_T38} media
         * type defined by RFC 4612.
         */
        public static final MediaType AUDIO_T38_TYPE =
                new MediaType("audio", "t38");

        /**
         * A {@code String} constant representing {@value #AUDIO_TELEPHONE_EVENT} media
         * type defined by RFC 4733.
         */
        public static final String AUDIO_TELEPHONE_EVENT =
                "audio/telephone-event";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_TELEPHONE_EVENT} media
         * type defined by RFC 4733.
         */
        public static final MediaType AUDIO_TELEPHONE_EVENT_TYPE =
                new MediaType("audio", "telephone-event");

        /**
         * A {@code String} constant representing {@value #AUDIO_TONE} media
         * type defined by RFC 4733.
         */
        public static final String AUDIO_TONE =
                "audio/tone";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_TONE} media
         * type defined by RFC 4733.
         */
        public static final MediaType AUDIO_TONE_TYPE =
                new MediaType("audio", "tone");

        /**
         * A {@code String} constant representing {@value #AUDIO_TSVCIS} media
         * type defined by RFC 8817.
         */
        public static final String AUDIO_TSVCIS =
                "audio/TSVCIS";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_TSVCIS} media
         * type defined by RFC 8817.
         */
        public static final MediaType AUDIO_TSVCIS_TYPE =
                new MediaType("audio", "TSVCIS");

        /**
         * A {@code String} constant representing {@value #AUDIO_UEMCLIP} media
         * type defined by RFC 5686.
         */
        public static final String AUDIO_UEMCLIP =
                "audio/UEMCLIP";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_UEMCLIP} media
         * type defined by RFC 5686.
         */
        public static final MediaType AUDIO_UEMCLIP_TYPE =
                new MediaType("audio", "UEMCLIP");

        /**
         * A {@code String} constant representing {@value #AUDIO_ULPFEC} media
         * type defined by RFC 5109.
         */
        public static final String AUDIO_ULPFEC =
                "audio/ulpfec";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_ULPFEC} media
         * type defined by RFC 5109.
         */
        public static final MediaType AUDIO_ULPFEC_TYPE =
                new MediaType("audio", "ulpfec");

        /**
         * A {@code String} constant representing {@value #AUDIO_VDVI} media
         * type defined by RFC 4856.
         */
        public static final String AUDIO_VDVI =
                "audio/VDVI";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_VDVI} media
         * type defined by RFC 4856.
         */
        public static final MediaType AUDIO_VDVI_TYPE =
                new MediaType("audio", "VDVI");

        /**
         * A {@code String} constant representing {@value #AUDIO_VMR_WB} media
         * type defined by RFC 4348, and RFC 4424.
         */
        public static final String AUDIO_VMR_WB =
                "audio/VMR-WB";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_VMR_WB} media
         * type defined by RFC 4348, and RFC 4424.
         */
        public static final MediaType AUDIO_VMR_WB_TYPE =
                new MediaType("audio", "VMR-WB");

        /**
         * A {@code String} constant representing {@value #AUDIO_VND_QCELP} media
         * type defined by RFC 3625.
         */
        public static final String AUDIO_VND_QCELP =
                "audio/vnd.qcelp";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_VND_QCELP} media
         * type defined by RFC 3625.
         */
        public static final MediaType AUDIO_VND_QCELP_TYPE =
                new MediaType("audio", "vnd.qcelp");

        /**
         * A {@code String} constant representing {@value #AUDIO_VORBIS} media
         * type defined by RFC 5215.
         */
        public static final String AUDIO_VORBIS =
                "audio/vorbis";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_VORBIS} media
         * type defined by RFC 5215.
         */
        public static final MediaType AUDIO_VORBIS_TYPE =
                new MediaType("audio", "vorbis");

        /**
         * A {@code String} constant representing {@value #AUDIO_VORBIS_CONFIG} media
         * type defined by RFC 5215.
         */
        public static final String AUDIO_VORBIS_CONFIG =
                "audio/vorbis-config";

        /**
         * A {@link MediaType} constant representing {@value #AUDIO_VORBIS_CONFIG} media
         * type defined by RFC 5215.
         */
        public static final MediaType AUDIO_VORBIS_CONFIG_TYPE =
                new MediaType("audio", "vorbis-config");
    }

    /**
     * Font type media subtypes.
     */
    public static class Font {
        /**
         * A {@code String} constant representing {@value #FONT_COLLECTION} media
         * type defined by RFC 8081.
         */
        public static final String FONT_COLLECTION =
                "font/collection";

        /**
         * A {@link MediaType} constant representing {@value #FONT_COLLECTION} media
         * type defined by RFC 8081.
         */
        public static final MediaType FONT_COLLECTION_TYPE =
                new MediaType("font", "collection");

        /**
         * A {@code String} constant representing {@value #FONT_OTF} media
         * type defined by RFC 8081.
         */
        public static final String FONT_OTF =
                "font/otf";

        /**
         * A {@link MediaType} constant representing {@value #FONT_OTF} media
         * type defined by RFC 8081.
         */
        public static final MediaType FONT_OTF_TYPE =
                new MediaType("font", "otf");

        /**
         * A {@code String} constant representing {@value #FONT_SFNT} media
         * type defined by RFC 8081.
         */
        public static final String FONT_SFNT =
                "font/sfnt";

        /**
         * A {@link MediaType} constant representing {@value #FONT_SFNT} media
         * type defined by RFC 8081.
         */
        public static final MediaType FONT_SFNT_TYPE =
                new MediaType("font", "sfnt");

        /**
         * A {@code String} constant representing {@value #FONT_TTF} media
         * type defined by RFC 8081.
         */
        public static final String FONT_TTF =
                "font/ttf";

        /**
         * A {@link MediaType} constant representing {@value #FONT_TTF} media
         * type defined by RFC 8081.
         */
        public static final MediaType FONT_TTF_TYPE =
                new MediaType("font", "ttf");

        /**
         * A {@code String} constant representing {@value #FONT_WOFF} media
         * type defined by RFC 8081.
         */
        public static final String FONT_WOFF =
                "font/woff";

        /**
         * A {@link MediaType} constant representing {@value #FONT_WOFF} media
         * type defined by RFC 8081.
         */
        public static final MediaType FONT_WOFF_TYPE =
                new MediaType("font", "woff");

        /**
         * A {@code String} constant representing {@value #FONT_WOFF2} media
         * type defined by RFC 8081.
         */
        public static final String FONT_WOFF2 =
                "font/woff2";

        /**
         * A {@link MediaType} constant representing {@value #FONT_WOFF2} media
         * type defined by RFC 8081.
         */
        public static final MediaType FONT_WOFF2_TYPE =
                new MediaType("font", "woff2");
    }

    /**
     * Haptics type media subtypes.
     */
    public static class Haptics {
        /**
         * A {@code String} constant representing {@value #HAPTICS_IVS} media
         * type defined by RFC 9695.
         */
        public static final String HAPTICS_IVS =
                "haptics/ivs";

        /**
         * A {@link MediaType} constant representing {@value #HAPTICS_IVS} media
         * type defined by RFC 9695.
         */
        public static final MediaType HAPTICS_IVS_TYPE =
                new MediaType("haptics", "ivs");

        /**
         * A {@code String} constant representing {@value #HAPTICS_HJIF} media
         * type defined by RFC 9695.
         */
        public static final String HAPTICS_HJIF =
                "haptics/hjif";

        /**
         * A {@link MediaType} constant representing {@value #HAPTICS_HJIF} media
         * type defined by RFC 9695.
         */
        public static final MediaType HAPTICS_HJIF_TYPE =
                new MediaType("haptics", "hjif");

        /**
         * A {@code String} constant representing {@value #HAPTICS_HMPG} media
         * type defined by RFC 9695.
         */
        public static final String HAPTICS_HMPG =
                "haptics/hmpg";

        /**
         * A {@link MediaType} constant representing {@value #HAPTICS_HMPG} media
         * type defined by RFC 9695.
         */
        public static final MediaType HAPTICS_HMPG_TYPE =
                new MediaType("haptics", "hmpg");
    }

    /**
     * Image type media subtypes.
     */
    public static class Image {
        /**
         * A {@code String} constant representing {@value #IMAGE_BMP} media
         * type defined by RFC 7903.
         */
        public static final String IMAGE_BMP =
                "image/bmp";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_BMP} media
         * type defined by RFC 7903.
         */
        public static final MediaType IMAGE_BMP_TYPE =
                new MediaType("image", "bmp");

        /**
         * A {@code String} constant representing {@value #IMAGE_EMF} media
         * type defined by RFC 7903.
         */
        public static final String IMAGE_EMF =
                "image/emf";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_EMF} media
         * type defined by RFC 7903.
         */
        public static final MediaType IMAGE_EMF_TYPE =
                new MediaType("image", "emf");

        /**
         * A {@code String} constant representing {@value #IMAGE_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final String IMAGE_EXAMPLE =
                "image/example";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final MediaType IMAGE_EXAMPLE_TYPE =
                new MediaType("image", "example");

        /**
         * A {@code String} constant representing {@value #IMAGE_FITS} media
         * type defined by RFC 4047.
         */
        public static final String IMAGE_FITS =
                "image/fits";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_FITS} media
         * type defined by RFC 4047.
         */
        public static final MediaType IMAGE_FITS_TYPE =
                new MediaType("image", "fits");

        /**
         * A {@code String} constant representing {@value #IMAGE_G3FAX} media
         * type defined by RFC 1494.
         */
        public static final String IMAGE_G3FAX =
                "image/g3fax";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_G3FAX} media
         * type defined by RFC 1494.
         */
        public static final MediaType IMAGE_G3FAX_TYPE =
                new MediaType("image", "g3fax");

        /**
         * A {@code String} constant representing {@value #IMAGE_GIF} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final String IMAGE_GIF =
                "image/gif";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_GIF} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final MediaType IMAGE_GIF_TYPE =
                new MediaType("image", "gif");

        /**
         * A {@code String} constant representing {@value #IMAGE_IEF} media
         * type defined by RFC 1314.
         */
        public static final String IMAGE_IEF =
                "image/ief";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_IEF} media
         * type defined by RFC 1314.
         */
        public static final MediaType IMAGE_IEF_TYPE =
                new MediaType("image", "ief");

        /**
         * A {@code String} constant representing {@value #IMAGE_JP2} media
         * type defined by RFC 3745.
         */
        public static final String IMAGE_JP2 =
                "image/jp2";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_JP2} media
         * type defined by RFC 3745.
         */
        public static final MediaType IMAGE_JP2_TYPE =
                new MediaType("image", "jp2");

        /**
         * A {@code String} constant representing {@value #IMAGE_JPEG} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final String IMAGE_JPEG =
                "image/jpeg";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_JPEG} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final MediaType IMAGE_JPEG_TYPE =
                new MediaType("image", "jpeg");

        /**
         * A {@code String} constant representing {@value #IMAGE_JPM} media
         * type defined by RFC 3745.
         */
        public static final String IMAGE_JPM =
                "image/jpm";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_JPM} media
         * type defined by RFC 3745.
         */
        public static final MediaType IMAGE_JPM_TYPE =
                new MediaType("image", "jpm");

        /**
         * A {@code String} constant representing {@value #IMAGE_JPX} media
         * type defined by RFC 3745.
         */
        public static final String IMAGE_JPX =
                "image/jpx";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_JPX} media
         * type defined by RFC 3745.
         */
        public static final MediaType IMAGE_JPX_TYPE =
                new MediaType("image", "jpx");

        /**
         * A {@code String} constant representing {@value #IMAGE_SVG_XML} media
         * type defined by {@code http://www.w3.org/TR/SVG/mimereg.html}.
         */
        public static final String IMAGE_SVG_XML =
                "image/svg+xml";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_SVG_XML} media
         * type defined by {@code http://www.w3.org/TR/SVG/mimereg.html}.
         */
        public static final MediaType IMAGE_SVG_XML_TYPE =
                new MediaType("image", "svg+xml");

        /**
         * A {@code String} constant representing {@value #IMAGE_T38} media
         * type defined by RFC 3362.
         */
        public static final String IMAGE_T38 =
                "image/t38";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_T38} media
         * type defined by RFC 3362.
         */
        public static final MediaType IMAGE_T38_TYPE =
                new MediaType("image", "t38");

        /**
         * A {@code String} constant representing {@value #IMAGE_TIFF} media
         * type defined by RFC 3302.
         */
        public static final String IMAGE_TIFF =
                "image/tiff";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_TIFF} media
         * type defined by RFC 3302.
         */
        public static final MediaType IMAGE_TIFF_TYPE =
                new MediaType("image", "tiff");

        /**
         * A {@code String} constant representing {@value #IMAGE_TIFF_FX} media
         * type defined by RFC 3950.
         */
        public static final String IMAGE_TIFF_FX =
                "image/tiff-fx";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_TIFF_FX} media
         * type defined by RFC 3950.
         */
        public static final MediaType IMAGE_TIFF_FX_TYPE =
                new MediaType("image", "tiff-fx");

        /**
         * A {@code String} constant representing {@value #IMAGE_WEBP} media
         * type defined by RFC 9649.
         */
        public static final String IMAGE_WEBP =
                "image/webp";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_WEBP} media
         * type defined by RFC 9649.
         */
        public static final MediaType IMAGE_WEBP_TYPE =
                new MediaType("image", "webp");

        /**
         * A {@code String} constant representing {@value #IMAGE_WMF} media
         * type defined by RFC 7903.
         */
        public static final String IMAGE_WMF =
                "image/wmf";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_WMF} media
         * type defined by RFC 7903.
         */
        public static final MediaType IMAGE_WMF_TYPE =
                new MediaType("image", "wmf");

        /**
         * A {@code String} constant representing {@value #IMAGE_X_EMF} media
         * type defined by RFC 7903.
         */
        public static final String IMAGE_X_EMF =
                "image/x-emf";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_X_EMF} media
         * type defined by RFC 7903.
         */
        public static final MediaType IMAGE_X_EMF_TYPE =
                new MediaType("image", "x-emf");

        /**
         * A {@code String} constant representing {@value #IMAGE_X_WMF} media
         * type defined by RFC 7903.
         */
        public static final String IMAGE_X_WMF =
                "image/x-wmf";

        /**
         * A {@link MediaType} constant representing {@value #IMAGE_X_WMF} media
         * type defined by RFC 7903.
         */
        public static final MediaType IMAGE_X_WMF_TYPE =
                new MediaType("image", "x-wmf");
    }

    /**
     * Message type media subtypes.
     */
    public static class Message {
        /**
         * A {@code String} constant representing {@value #MESSAGE_BHTTP} media
         * type defined by RFC 9292.
         */
        public static final String MESSAGE_BHTTP =
                "message/bhttp";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_BHTTP} media
         * type defined by RFC 9292.
         */
        public static final MediaType MESSAGE_BHTTP_TYPE =
                new MediaType("message", "bhttp");

        /**
         * A {@code String} constant representing {@value #MESSAGE_CPIM} media
         * type defined by RFC 3862.
         */
        public static final String MESSAGE_CPIM =
                "message/CPIM";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_CPIM} media
         * type defined by RFC 3862.
         */
        public static final MediaType MESSAGE_CPIM_TYPE =
                new MediaType("message", "CPIM");

        /**
         * A {@code String} constant representing {@value #MESSAGE_DELIVERY_STATUS} media
         * type defined by RFC 1894.
         */
        public static final String MESSAGE_DELIVERY_STATUS =
                "message/delivery-status";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_DELIVERY_STATUS} media
         * type defined by RFC 1894.
         */
        public static final MediaType MESSAGE_DELIVERY_STATUS_TYPE =
                new MediaType("message", "delivery-status");

        /**
         * A {@code String} constant representing {@value #MESSAGE_DISPOSITION_NOTIFICATION} media
         * type defined by RFC 8098.
         */
        public static final String MESSAGE_DISPOSITION_NOTIFICATION =
                "message/disposition-notification";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_DISPOSITION_NOTIFICATION} media
         * type defined by RFC 8098.
         */
        public static final MediaType MESSAGE_DISPOSITION_NOTIFICATION_TYPE =
                new MediaType("message", "disposition-notification");

        /**
         * A {@code String} constant representing {@value #MESSAGE_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final String MESSAGE_EXAMPLE =
                "message/example";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final MediaType MESSAGE_EXAMPLE_TYPE =
                new MediaType("message", "example");

        /**
         * A {@code String} constant representing {@value #MESSAGE_EXTERNAL_BODY} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final String MESSAGE_EXTERNAL_BODY =
                "message/external-body";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_EXTERNAL_BODY} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final MediaType MESSAGE_EXTERNAL_BODY_TYPE =
                new MediaType("message", "external-body");

        /**
         * A {@code String} constant representing {@value #MESSAGE_FEEDBACK_REPORT} media
         * type defined by RFC 5965.
         */
        public static final String MESSAGE_FEEDBACK_REPORT =
                "message/feedback-report";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_FEEDBACK_REPORT} media
         * type defined by RFC 5965.
         */
        public static final MediaType MESSAGE_FEEDBACK_REPORT_TYPE =
                new MediaType("message", "feedback-report");

        /**
         * A {@code String} constant representing {@value #MESSAGE_GLOBAL} media
         * type defined by RFC 6532.
         */
        public static final String MESSAGE_GLOBAL =
                "message/global";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_GLOBAL} media
         * type defined by RFC 6532.
         */
        public static final MediaType MESSAGE_GLOBAL_TYPE =
                new MediaType("message", "global");

        /**
         * A {@code String} constant representing {@value #MESSAGE_GLOBAL_DELIVERY_STATUS} media
         * type defined by RFC 6533.
         */
        public static final String MESSAGE_GLOBAL_DELIVERY_STATUS =
                "message/global-delivery-status";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_GLOBAL_DELIVERY_STATUS} media
         * type defined by RFC 6533.
         */
        public static final MediaType MESSAGE_GLOBAL_DELIVERY_STATUS_TYPE =
                new MediaType("message", "global-delivery-status");

        /**
         * A {@code String} constant representing {@value #MESSAGE_GLOBAL_DISPOSITION_NOTIFICATION} media
         * type defined by RFC 6533.
         */
        public static final String MESSAGE_GLOBAL_DISPOSITION_NOTIFICATION =
                "message/global-disposition-notification";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_GLOBAL_DISPOSITION_NOTIFICATION} media
         * type defined by RFC 6533.
         */
        public static final MediaType MESSAGE_GLOBAL_DISPOSITION_NOTIFICATION_TYPE =
                new MediaType("message", "global-disposition-notification");

        /**
         * A {@code String} constant representing {@value #MESSAGE_GLOBAL_HEADERS} media
         * type defined by RFC 6533.
         */
        public static final String MESSAGE_GLOBAL_HEADERS =
                "message/global-headers";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_GLOBAL_HEADERS} media
         * type defined by RFC 6533.
         */
        public static final MediaType MESSAGE_GLOBAL_HEADERS_TYPE =
                new MediaType("message", "global-headers");

        /**
         * A {@code String} constant representing {@value #MESSAGE_HTTP} media
         * type defined by RFC 9112.
         */
        public static final String MESSAGE_HTTP =
                "message/http";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_HTTP} media
         * type defined by RFC 9112.
         */
        public static final MediaType MESSAGE_HTTP_TYPE =
                new MediaType("message", "http");

        /**
         * A {@code String} constant representing {@value #MESSAGE_IMDN_XML} media
         * type defined by RFC 5438.
         */
        public static final String MESSAGE_IMDN_XML =
                "message/imdn+xml";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_IMDN_XML} media
         * type defined by RFC 5438.
         */
        public static final MediaType MESSAGE_IMDN_XML_TYPE =
                new MediaType("message", "imdn+xml");

        /**
         * A {@code String} constant representing {@value #MESSAGE_MLS} media
         * type defined by RFC 9420.
         */
        public static final String MESSAGE_MLS =
                "message/mls";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_MLS} media
         * type defined by RFC 9420.
         */
        public static final MediaType MESSAGE_MLS_TYPE =
                new MediaType("message", "mls");

        /**
         * A {@code String} constant representing {@value #MESSAGE_NEWS} media
         * type defined by RFC 5537.
         */
        public static final String MESSAGE_NEWS =
                "message/news";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_NEWS} media
         * type defined by RFC 5537.
         */
        public static final MediaType MESSAGE_NEWS_TYPE =
                new MediaType("message", "news");

        /**
         * A {@code String} constant representing {@value #MESSAGE_OHTTP_REQ} media
         * type defined by RFC 9458.
         */
        public static final String MESSAGE_OHTTP_REQ =
                "message/ohttp-req";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_OHTTP_REQ} media
         * type defined by RFC 9458.
         */
        public static final MediaType MESSAGE_OHTTP_REQ_TYPE =
                new MediaType("message", "ohttp-req");

        /**
         * A {@code String} constant representing {@value #MESSAGE_OHTTP_RES} media
         * type defined by RFC 9458.
         */
        public static final String MESSAGE_OHTTP_RES =
                "message/ohttp-res";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_OHTTP_RES} media
         * type defined by RFC 9458.
         */
        public static final MediaType MESSAGE_OHTTP_RES_TYPE =
                new MediaType("message", "ohttp-res");

        /**
         * A {@code String} constant representing {@value #MESSAGE_PARTIAL} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final String MESSAGE_PARTIAL =
                "message/partial";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_PARTIAL} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final MediaType MESSAGE_PARTIAL_TYPE =
                new MediaType("message", "partial");

        /**
         * A {@code String} constant representing {@value #MESSAGE_RFC822} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final String MESSAGE_RFC822 =
                "message/rfc822";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_RFC822} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final MediaType MESSAGE_RFC822_TYPE =
                new MediaType("message", "rfc822");

        /**
         * A {@code String} constant representing {@value #MESSAGE_S_HTTP} media
         * type defined by RFC 2660{@code Status change of HTTP experiments to Historic}.
         */
        public static final String MESSAGE_S_HTTP =
                "message/s-http";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_S_HTTP} media
         * type defined by RFC 2660{@code Status change of HTTP experiments to Historic}.
         */
        public static final MediaType MESSAGE_S_HTTP_TYPE =
                new MediaType("message", "s-http");

        /**
         * A {@code String} constant representing {@value #MESSAGE_SIP} media
         * type defined by RFC 3261.
         */
        public static final String MESSAGE_SIP =
                "message/sip";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_SIP} media
         * type defined by RFC 3261.
         */
        public static final MediaType MESSAGE_SIP_TYPE =
                new MediaType("message", "sip");

        /**
         * A {@code String} constant representing {@value #MESSAGE_SIPFRAG} media
         * type defined by RFC 3420.
         */
        public static final String MESSAGE_SIPFRAG =
                "message/sipfrag";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_SIPFRAG} media
         * type defined by RFC 3420.
         */
        public static final MediaType MESSAGE_SIPFRAG_TYPE =
                new MediaType("message", "sipfrag");

        /**
         * A {@code String} constant representing {@value #MESSAGE_TRACKING_STATUS} media
         * type defined by RFC 3886.
         */
        public static final String MESSAGE_TRACKING_STATUS =
                "message/tracking-status";

        /**
         * A {@link MediaType} constant representing {@value #MESSAGE_TRACKING_STATUS} media
         * type defined by RFC 3886.
         */
        public static final MediaType MESSAGE_TRACKING_STATUS_TYPE =
                new MediaType("message", "tracking-status");
    }

    /**
     * Model type media subtypes.
     */
    public static class Model {
        /**
         * A {@code String} constant representing {@value #MODEL_3MF} media
         * type defined by {@code http://www.3mf.io/specification}.
         */
        public static final String MODEL_3MF =
                "model/3mf";

        /**
         * A {@link MediaType} constant representing {@value #MODEL_3MF} media
         * type defined by {@code http://www.3mf.io/specification}.
         */
        public static final MediaType MODEL_3MF_TYPE =
                new MediaType("model", "3mf");

        /**
         * A {@code String} constant representing {@value #MODEL_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final String MODEL_EXAMPLE =
                "model/example";

        /**
         * A {@link MediaType} constant representing {@value #MODEL_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final MediaType MODEL_EXAMPLE_TYPE =
                new MediaType("model", "example");

        /**
         * A {@code String} constant representing {@value #MODEL_MESH} media
         * type defined by RFC 2077.
         */
        public static final String MODEL_MESH =
                "model/mesh";

        /**
         * A {@link MediaType} constant representing {@value #MODEL_MESH} media
         * type defined by RFC 2077.
         */
        public static final MediaType MODEL_MESH_TYPE =
                new MediaType("model", "mesh");

        /**
         * A {@code String} constant representing {@value #MODEL_VRML} media
         * type defined by RFC 2077.
         */
        public static final String MODEL_VRML =
                "model/vrml";

        /**
         * A {@link MediaType} constant representing {@value #MODEL_VRML} media
         * type defined by RFC 2077.
         */
        public static final MediaType MODEL_VRML_TYPE =
                new MediaType("model", "vrml");
    }

    /**
     * Multipart type media subtypes.
     */
    public static class Multipart {
        /**
         * A {@code String} constant representing {@value #MULTIPART_ALTERNATIVE} media
         * type defined by RFC 2046, and RFC 2045.
         */
        public static final String MULTIPART_ALTERNATIVE =
                "multipart/alternative";

        /**
         * A {@link MediaType} constant representing {@value #MULTIPART_ALTERNATIVE} media
         * type defined by RFC 2046, and RFC 2045.
         */
        public static final MediaType MULTIPART_ALTERNATIVE_TYPE =
                new MediaType("multipart", "alternative");

        /**
         * A {@code String} constant representing {@value #MULTIPART_BYTERANGES} media
         * type defined by RFC 9110.
         */
        public static final String MULTIPART_BYTERANGES =
                "multipart/byteranges";

        /**
         * A {@link MediaType} constant representing {@value #MULTIPART_BYTERANGES} media
         * type defined by RFC 9110.
         */
        public static final MediaType MULTIPART_BYTERANGES_TYPE =
                new MediaType("multipart", "byteranges");

        /**
         * A {@code String} constant representing {@value #MULTIPART_DIGEST} media
         * type defined by RFC 2046, and RFC 2045.
         */
        public static final String MULTIPART_DIGEST =
                "multipart/digest";

        /**
         * A {@link MediaType} constant representing {@value #MULTIPART_DIGEST} media
         * type defined by RFC 2046, and RFC 2045.
         */
        public static final MediaType MULTIPART_DIGEST_TYPE =
                new MediaType("multipart", "digest");

        /**
         * A {@code String} constant representing {@value #MULTIPART_ENCRYPTED} media
         * type defined by RFC 1847.
         */
        public static final String MULTIPART_ENCRYPTED =
                "multipart/encrypted";

        /**
         * A {@link MediaType} constant representing {@value #MULTIPART_ENCRYPTED} media
         * type defined by RFC 1847.
         */
        public static final MediaType MULTIPART_ENCRYPTED_TYPE =
                new MediaType("multipart", "encrypted");

        /**
         * A {@code String} constant representing {@value #MULTIPART_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final String MULTIPART_EXAMPLE =
                "multipart/example";

        /**
         * A {@link MediaType} constant representing {@value #MULTIPART_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final MediaType MULTIPART_EXAMPLE_TYPE =
                new MediaType("multipart", "example");

        /**
         * A {@code String} constant representing {@value #MULTIPART_FORM_DATA} media
         * type defined by RFC 7578.
         */
        public static final String MULTIPART_FORM_DATA =
                "multipart/form-data";

        /**
         * A {@link MediaType} constant representing {@value #MULTIPART_FORM_DATA} media
         * type defined by RFC 7578.
         */
        public static final MediaType MULTIPART_FORM_DATA_TYPE =
                new MediaType("multipart", "form-data");

        /**
         * A {@code String} constant representing {@value #MULTIPART_MIXED} media
         * type defined by RFC 2046, and RFC 2045.
         */
        public static final String MULTIPART_MIXED =
                "multipart/mixed";

        /**
         * A {@link MediaType} constant representing {@value #MULTIPART_MIXED} media
         * type defined by RFC 2046, and RFC 2045.
         */
        public static final MediaType MULTIPART_MIXED_TYPE =
                new MediaType("multipart", "mixed");

        /**
         * A {@code String} constant representing {@value #MULTIPART_MULTILINGUAL} media
         * type defined by RFC 8255.
         */
        public static final String MULTIPART_MULTILINGUAL =
                "multipart/multilingual";

        /**
         * A {@link MediaType} constant representing {@value #MULTIPART_MULTILINGUAL} media
         * type defined by RFC 8255.
         */
        public static final MediaType MULTIPART_MULTILINGUAL_TYPE =
                new MediaType("multipart", "multilingual");

        /**
         * A {@code String} constant representing {@value #MULTIPART_PARALLEL} media
         * type defined by RFC 2046, and RFC 2045.
         */
        public static final String MULTIPART_PARALLEL =
                "multipart/parallel";

        /**
         * A {@link MediaType} constant representing {@value #MULTIPART_PARALLEL} media
         * type defined by RFC 2046, and RFC 2045.
         */
        public static final MediaType MULTIPART_PARALLEL_TYPE =
                new MediaType("multipart", "parallel");

        /**
         * A {@code String} constant representing {@value #MULTIPART_RELATED} media
         * type defined by RFC 2387.
         */
        public static final String MULTIPART_RELATED =
                "multipart/related";

        /**
         * A {@link MediaType} constant representing {@value #MULTIPART_RELATED} media
         * type defined by RFC 2387.
         */
        public static final MediaType MULTIPART_RELATED_TYPE =
                new MediaType("multipart", "related");

        /**
         * A {@code String} constant representing {@value #MULTIPART_REPORT} media
         * type defined by RFC 6522.
         */
        public static final String MULTIPART_REPORT =
                "multipart/report";

        /**
         * A {@link MediaType} constant representing {@value #MULTIPART_REPORT} media
         * type defined by RFC 6522.
         */
        public static final MediaType MULTIPART_REPORT_TYPE =
                new MediaType("multipart", "report");

        /**
         * A {@code String} constant representing {@value #MULTIPART_SIGNED} media
         * type defined by RFC 1847.
         */
        public static final String MULTIPART_SIGNED =
                "multipart/signed";

        /**
         * A {@link MediaType} constant representing {@value #MULTIPART_SIGNED} media
         * type defined by RFC 1847.
         */
        public static final MediaType MULTIPART_SIGNED_TYPE =
                new MediaType("multipart", "signed");

        /**
         * A {@code String} constant representing {@value #MULTIPART_VOICE_MESSAGE} media
         * type defined by RFC 3801.
         */
        public static final String MULTIPART_VOICE_MESSAGE =
                "multipart/voice-message";

        /**
         * A {@link MediaType} constant representing {@value #MULTIPART_VOICE_MESSAGE} media
         * type defined by RFC 3801.
         */
        public static final MediaType MULTIPART_VOICE_MESSAGE_TYPE =
                new MediaType("multipart", "voice-message");
    }

    /**
     * Text type media subtypes.
     */
    public static class Text {
        /**
         * A {@code String} constant representing {@value #TEXT_1D_INTERLEAVED_PARITYFEC} media
         * type defined by RFC 6015.
         */
        public static final String TEXT_1D_INTERLEAVED_PARITYFEC =
                "text/1d-interleaved-parityfec";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_1D_INTERLEAVED_PARITYFEC} media
         * type defined by RFC 6015.
         */
        public static final MediaType TEXT_1D_INTERLEAVED_PARITYFEC_TYPE =
                new MediaType("text", "1d-interleaved-parityfec");

        /**
         * A {@code String} constant representing {@value #TEXT_CALENDAR} media
         * type defined by RFC 5545.
         */
        public static final String TEXT_CALENDAR =
                "text/calendar";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_CALENDAR} media
         * type defined by RFC 5545.
         */
        public static final MediaType TEXT_CALENDAR_TYPE =
                new MediaType("text", "calendar");

        /**
         * A {@code String} constant representing {@value #TEXT_CSS} media
         * type defined by RFC 2318.
         */
        public static final String TEXT_CSS =
                "text/css";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_CSS} media
         * type defined by RFC 2318.
         */
        public static final MediaType TEXT_CSS_TYPE =
                new MediaType("text", "css");

        /**
         * A {@code String} constant representing {@value #TEXT_CSV} media
         * type defined by RFC 4180, and RFC 7111.
         */
        public static final String TEXT_CSV =
                "text/csv";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_CSV} media
         * type defined by RFC 4180, and RFC 7111.
         */
        public static final MediaType TEXT_CSV_TYPE =
                new MediaType("text", "csv");

        /**
         * A {@code String} constant representing {@value #TEXT_DIRECTORY} media
         * type defined by RFC 2425, and RFC 6350.
         */
        public static final String TEXT_DIRECTORY =
                "text/directory";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_DIRECTORY} media
         * type defined by RFC 2425, and RFC 6350.
         */
        public static final MediaType TEXT_DIRECTORY_TYPE =
                new MediaType("text", "directory");

        /**
         * A {@code String} constant representing {@value #TEXT_DNS} media
         * type defined by RFC 4027.
         */
        public static final String TEXT_DNS =
                "text/dns";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_DNS} media
         * type defined by RFC 4027.
         */
        public static final MediaType TEXT_DNS_TYPE =
                new MediaType("text", "dns");

        /**
         * A {@code String} constant representing {@value #TEXT_ECMASCRIPT} media
         * type defined by RFC 9239.
         */
        public static final String TEXT_ECMASCRIPT =
                "text/ecmascript";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_ECMASCRIPT} media
         * type defined by RFC 9239.
         */
        public static final MediaType TEXT_ECMASCRIPT_TYPE =
                new MediaType("text", "ecmascript");

        /**
         * A {@code String} constant representing {@value #TEXT_ENCAPRTP} media
         * type defined by RFC 6849.
         */
        public static final String TEXT_ENCAPRTP =
                "text/encaprtp";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_ENCAPRTP} media
         * type defined by RFC 6849.
         */
        public static final MediaType TEXT_ENCAPRTP_TYPE =
                new MediaType("text", "encaprtp");

        /**
         * A {@code String} constant representing {@value #TEXT_ENRICHED} media
         * type defined by RFC 1896.
         */
        public static final String TEXT_ENRICHED =
                "text/enriched";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_ENRICHED} media
         * type defined by RFC 1896.
         */
        public static final MediaType TEXT_ENRICHED_TYPE =
                new MediaType("text", "enriched");

        /**
         * A {@code String} constant representing {@value #TEXT_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final String TEXT_EXAMPLE =
                "text/example";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final MediaType TEXT_EXAMPLE_TYPE =
                new MediaType("text", "example");

        /**
         * A {@code String} constant representing {@value #TEXT_FLEXFEC} media
         * type defined by RFC 8627.
         */
        public static final String TEXT_FLEXFEC =
                "text/flexfec";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_FLEXFEC} media
         * type defined by RFC 8627.
         */
        public static final MediaType TEXT_FLEXFEC_TYPE =
                new MediaType("text", "flexfec");

        /**
         * A {@code String} constant representing {@value #TEXT_FWDRED} media
         * type defined by RFC 6354.
         */
        public static final String TEXT_FWDRED =
                "text/fwdred";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_FWDRED} media
         * type defined by RFC 6354.
         */
        public static final MediaType TEXT_FWDRED_TYPE =
                new MediaType("text", "fwdred");

        /**
         * A {@code String} constant representing {@value #TEXT_GRAMMAR_REF_LIST} media
         * type defined by RFC 6787.
         */
        public static final String TEXT_GRAMMAR_REF_LIST =
                "text/grammar-ref-list";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_GRAMMAR_REF_LIST} media
         * type defined by RFC 6787.
         */
        public static final MediaType TEXT_GRAMMAR_REF_LIST_TYPE =
                new MediaType("text", "grammar-ref-list");

        /**
         * A {@code String} constant representing {@value #TEXT_JAVASCRIPT} media
         * type defined by RFC 9239.
         */
        public static final String TEXT_JAVASCRIPT =
                "text/javascript";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_JAVASCRIPT} media
         * type defined by RFC 9239.
         */
        public static final MediaType TEXT_JAVASCRIPT_TYPE =
                new MediaType("text", "javascript");

        /**
         * A {@code String} constant representing {@value #TEXT_MARKDOWN} media
         * type defined by RFC 7763.
         */
        public static final String TEXT_MARKDOWN =
                "text/markdown";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_MARKDOWN} media
         * type defined by RFC 7763.
         */
        public static final MediaType TEXT_MARKDOWN_TYPE =
                new MediaType("text", "markdown");

        /**
         * A {@code String} constant representing {@value #TEXT_PARAMETERS} media
         * type defined by RFC 7826.
         */
        public static final String TEXT_PARAMETERS =
                "text/parameters";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_PARAMETERS} media
         * type defined by RFC 7826.
         */
        public static final MediaType TEXT_PARAMETERS_TYPE =
                new MediaType("text", "parameters");

        /**
         * A {@code String} constant representing {@value #TEXT_PARITYFEC} media
         * type defined by RFC 3009.
         */
        public static final String TEXT_PARITYFEC =
                "text/parityfec";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_PARITYFEC} media
         * type defined by RFC 3009.
         */
        public static final MediaType TEXT_PARITYFEC_TYPE =
                new MediaType("text", "parityfec");

        /**
         * A {@code String} constant representing {@value #TEXT_PLAIN} media
         * type defined by RFC 2046, and RFC 3676, and RFC 5147.
         */
        public static final String TEXT_PLAIN =
                "text/plain";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_PLAIN} media
         * type defined by RFC 2046, and RFC 3676, and RFC 5147.
         */
        public static final MediaType TEXT_PLAIN_TYPE =
                new MediaType("text", "plain");

        /**
         * A {@code String} constant representing {@value #TEXT_RAPTORFEC} media
         * type defined by RFC 6682.
         */
        public static final String TEXT_RAPTORFEC =
                "text/raptorfec";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_RAPTORFEC} media
         * type defined by RFC 6682.
         */
        public static final MediaType TEXT_RAPTORFEC_TYPE =
                new MediaType("text", "raptorfec");

        /**
         * A {@code String} constant representing {@value #TEXT_RED} media
         * type defined by RFC 4102.
         */
        public static final String TEXT_RED =
                "text/RED";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_RED} media
         * type defined by RFC 4102.
         */
        public static final MediaType TEXT_RED_TYPE =
                new MediaType("text", "RED");

        /**
         * A {@code String} constant representing {@value #TEXT_RFC822_HEADERS} media
         * type defined by RFC 6522.
         */
        public static final String TEXT_RFC822_HEADERS =
                "text/rfc822-headers";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_RFC822_HEADERS} media
         * type defined by RFC 6522.
         */
        public static final MediaType TEXT_RFC822_HEADERS_TYPE =
                new MediaType("text", "rfc822-headers");

        /**
         * A {@code String} constant representing {@value #TEXT_RICHTEXT} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final String TEXT_RICHTEXT =
                "text/richtext";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_RICHTEXT} media
         * type defined by RFC 2045,, and RFC 2046.
         */
        public static final MediaType TEXT_RICHTEXT_TYPE =
                new MediaType("text", "richtext");

        /**
         * A {@code String} constant representing {@value #TEXT_RTPLOOPBACK} media
         * type defined by RFC 6849.
         */
        public static final String TEXT_RTPLOOPBACK =
                "text/rtploopback";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_RTPLOOPBACK} media
         * type defined by RFC 6849.
         */
        public static final MediaType TEXT_RTPLOOPBACK_TYPE =
                new MediaType("text", "rtploopback");

        /**
         * A {@code String} constant representing {@value #TEXT_RTX} media
         * type defined by RFC 4588.
         */
        public static final String TEXT_RTX =
                "text/rtx";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_RTX} media
         * type defined by RFC 4588.
         */
        public static final MediaType TEXT_RTX_TYPE =
                new MediaType("text", "rtx");

        /**
         * A {@code String} constant representing {@value #TEXT_SGML} media
         * type defined by RFC 1874.
         */
        public static final String TEXT_SGML =
                "text/SGML";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_SGML} media
         * type defined by RFC 1874.
         */
        public static final MediaType TEXT_SGML_TYPE =
                new MediaType("text", "SGML");

        /**
         * A {@code String} constant representing {@value #TEXT_T140} media
         * type defined by RFC 4103.
         */
        public static final String TEXT_T140 =
                "text/t140";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_T140} media
         * type defined by RFC 4103.
         */
        public static final MediaType TEXT_T140_TYPE =
                new MediaType("text", "t140");

        /**
         * A {@code String} constant representing {@value #TEXT_TROFF} media
         * type defined by RFC 4263.
         */
        public static final String TEXT_TROFF =
                "text/troff";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_TROFF} media
         * type defined by RFC 4263.
         */
        public static final MediaType TEXT_TROFF_TYPE =
                new MediaType("text", "troff");

        /**
         * A {@code String} constant representing {@value #TEXT_ULPFEC} media
         * type defined by RFC 5109.
         */
        public static final String TEXT_ULPFEC =
                "text/ulpfec";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_ULPFEC} media
         * type defined by RFC 5109.
         */
        public static final MediaType TEXT_ULPFEC_TYPE =
                new MediaType("text", "ulpfec");

        /**
         * A {@code String} constant representing {@value #TEXT_URI_LIST} media
         * type defined by RFC 2483.
         */
        public static final String TEXT_URI_LIST =
                "text/uri-list";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_URI_LIST} media
         * type defined by RFC 2483.
         */
        public static final MediaType TEXT_URI_LIST_TYPE =
                new MediaType("text", "uri-list");

        /**
         * A {@code String} constant representing {@value #TEXT_VCARD} media
         * type defined by RFC 6350.
         */
        public static final String TEXT_VCARD =
                "text/vcard";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_VCARD} media
         * type defined by RFC 6350.
         */
        public static final MediaType TEXT_VCARD_TYPE =
                new MediaType("text", "vcard");

        /**
         * A {@code String} constant representing {@value #TEXT_VND_RADISYS_MSML_BASIC_LAYOUT} media
         * type defined by RFC 5707.
         */
        public static final String TEXT_VND_RADISYS_MSML_BASIC_LAYOUT =
                "text/vnd.radisys.msml-basic-layout";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_VND_RADISYS_MSML_BASIC_LAYOUT} media
         * type defined by RFC 5707.
         */
        public static final MediaType TEXT_VND_RADISYS_MSML_BASIC_LAYOUT_TYPE =
                new MediaType("text", "vnd.radisys.msml-basic-layout");

        /**
         * A {@code String} constant representing {@value #TEXT_XML} media
         * type defined by RFC 7303.
         */
        public static final String TEXT_XML =
                "text/xml";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_XML} media
         * type defined by RFC 7303.
         */
        public static final MediaType TEXT_XML_TYPE =
                new MediaType("text", "xml");

        /**
         * A {@code String} constant representing {@value #TEXT_XML_EXTERNAL_PARSED_ENTITY} media
         * type defined by RFC 7303.
         */
        public static final String TEXT_XML_EXTERNAL_PARSED_ENTITY =
                "text/xml-external-parsed-entity";

        /**
         * A {@link MediaType} constant representing {@value #TEXT_XML_EXTERNAL_PARSED_ENTITY} media
         * type defined by RFC 7303.
         */
        public static final MediaType TEXT_XML_EXTERNAL_PARSED_ENTITY_TYPE =
                new MediaType("text", "xml-external-parsed-entity");
    }

    /**
     * Video type media subtypes.
     */
    public static class Video {
        /**
         * A {@code String} constant representing {@value #VIDEO_1D_INTERLEAVED_PARITYFEC} media
         * type defined by RFC 6015.
         */
        public static final String VIDEO_1D_INTERLEAVED_PARITYFEC =
                "video/1d-interleaved-parityfec";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_1D_INTERLEAVED_PARITYFEC} media
         * type defined by RFC 6015.
         */
        public static final MediaType VIDEO_1D_INTERLEAVED_PARITYFEC_TYPE =
                new MediaType("video", "1d-interleaved-parityfec");

        /**
         * A {@code String} constant representing {@value #VIDEO_3GPP} media
         * type defined by RFC 3839, and RFC 6381.
         */
        public static final String VIDEO_3GPP =
                "video/3gpp";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_3GPP} media
         * type defined by RFC 3839, and RFC 6381.
         */
        public static final MediaType VIDEO_3GPP_TYPE =
                new MediaType("video", "3gpp");

        /**
         * A {@code String} constant representing {@value #VIDEO_3GPP2} media
         * type defined by RFC 4393, and RFC 6381.
         */
        public static final String VIDEO_3GPP2 =
                "video/3gpp2";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_3GPP2} media
         * type defined by RFC 4393, and RFC 6381.
         */
        public static final MediaType VIDEO_3GPP2_TYPE =
                new MediaType("video", "3gpp2");

        /**
         * A {@code String} constant representing {@value #VIDEO_3GPP_TT} media
         * type defined by RFC 4396.
         */
        public static final String VIDEO_3GPP_TT =
                "video/3gpp-tt";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_3GPP_TT} media
         * type defined by RFC 4396.
         */
        public static final MediaType VIDEO_3GPP_TT_TYPE =
                new MediaType("video", "3gpp-tt");

        /**
         * A {@code String} constant representing {@value #VIDEO_BMPEG} media
         * type defined by RFC 3555.
         */
        public static final String VIDEO_BMPEG =
                "video/BMPEG";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_BMPEG} media
         * type defined by RFC 3555.
         */
        public static final MediaType VIDEO_BMPEG_TYPE =
                new MediaType("video", "BMPEG");

        /**
         * A {@code String} constant representing {@value #VIDEO_BT656} media
         * type defined by RFC 3555.
         */
        public static final String VIDEO_BT656 =
                "video/BT656";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_BT656} media
         * type defined by RFC 3555.
         */
        public static final MediaType VIDEO_BT656_TYPE =
                new MediaType("video", "BT656");

        /**
         * A {@code String} constant representing {@value #VIDEO_CELB} media
         * type defined by RFC 3555.
         */
        public static final String VIDEO_CELB =
                "video/CelB";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_CELB} media
         * type defined by RFC 3555.
         */
        public static final MediaType VIDEO_CELB_TYPE =
                new MediaType("video", "CelB");

        /**
         * A {@code String} constant representing {@value #VIDEO_DV} media
         * type defined by RFC 6469.
         */
        public static final String VIDEO_DV =
                "video/DV";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_DV} media
         * type defined by RFC 6469.
         */
        public static final MediaType VIDEO_DV_TYPE =
                new MediaType("video", "DV");

        /**
         * A {@code String} constant representing {@value #VIDEO_ENCAPRTP} media
         * type defined by RFC 6849.
         */
        public static final String VIDEO_ENCAPRTP =
                "video/encaprtp";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_ENCAPRTP} media
         * type defined by RFC 6849.
         */
        public static final MediaType VIDEO_ENCAPRTP_TYPE =
                new MediaType("video", "encaprtp");

        /**
         * A {@code String} constant representing {@value #VIDEO_EVC} media
         * type defined by RFC 9584.
         */
        public static final String VIDEO_EVC =
                "video/evc";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_EVC} media
         * type defined by RFC 9584.
         */
        public static final MediaType VIDEO_EVC_TYPE =
                new MediaType("video", "evc");

        /**
         * A {@code String} constant representing {@value #VIDEO_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final String VIDEO_EXAMPLE =
                "video/example";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_EXAMPLE} media
         * type defined by RFC 4735.
         */
        public static final MediaType VIDEO_EXAMPLE_TYPE =
                new MediaType("video", "example");

        /**
         * A {@code String} constant representing {@value #VIDEO_FFV1} media
         * type defined by RFC 9043.
         */
        public static final String VIDEO_FFV1 =
                "video/FFV1";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_FFV1} media
         * type defined by RFC 9043.
         */
        public static final MediaType VIDEO_FFV1_TYPE =
                new MediaType("video", "FFV1");

        /**
         * A {@code String} constant representing {@value #VIDEO_FLEXFEC} media
         * type defined by RFC 8627.
         */
        public static final String VIDEO_FLEXFEC =
                "video/flexfec";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_FLEXFEC} media
         * type defined by RFC 8627.
         */
        public static final MediaType VIDEO_FLEXFEC_TYPE =
                new MediaType("video", "flexfec");

        /**
         * A {@code String} constant representing {@value #VIDEO_H261} media
         * type defined by RFC 4587.
         */
        public static final String VIDEO_H261 =
                "video/H261";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_H261} media
         * type defined by RFC 4587.
         */
        public static final MediaType VIDEO_H261_TYPE =
                new MediaType("video", "H261");

        /**
         * A {@code String} constant representing {@value #VIDEO_H263} media
         * type defined by RFC 3555.
         */
        public static final String VIDEO_H263 =
                "video/H263";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_H263} media
         * type defined by RFC 3555.
         */
        public static final MediaType VIDEO_H263_TYPE =
                new MediaType("video", "H263");

        /**
         * A {@code String} constant representing {@value #VIDEO_H263_1998} media
         * type defined by RFC 4629.
         */
        public static final String VIDEO_H263_1998 =
                "video/H263-1998";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_H263_1998} media
         * type defined by RFC 4629.
         */
        public static final MediaType VIDEO_H263_1998_TYPE =
                new MediaType("video", "H263-1998");

        /**
         * A {@code String} constant representing {@value #VIDEO_H263_2000} media
         * type defined by RFC 4629.
         */
        public static final String VIDEO_H263_2000 =
                "video/H263-2000";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_H263_2000} media
         * type defined by RFC 4629.
         */
        public static final MediaType VIDEO_H263_2000_TYPE =
                new MediaType("video", "H263-2000");

        /**
         * A {@code String} constant representing {@value #VIDEO_H264} media
         * type defined by RFC 6184.
         */
        public static final String VIDEO_H264 =
                "video/H264";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_H264} media
         * type defined by RFC 6184.
         */
        public static final MediaType VIDEO_H264_TYPE =
                new MediaType("video", "H264");

        /**
         * A {@code String} constant representing {@value #VIDEO_H264_RCDO} media
         * type defined by RFC 6185.
         */
        public static final String VIDEO_H264_RCDO =
                "video/H264-RCDO";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_H264_RCDO} media
         * type defined by RFC 6185.
         */
        public static final MediaType VIDEO_H264_RCDO_TYPE =
                new MediaType("video", "H264-RCDO");

        /**
         * A {@code String} constant representing {@value #VIDEO_H264_SVC} media
         * type defined by RFC 6190.
         */
        public static final String VIDEO_H264_SVC =
                "video/H264-SVC";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_H264_SVC} media
         * type defined by RFC 6190.
         */
        public static final MediaType VIDEO_H264_SVC_TYPE =
                new MediaType("video", "H264-SVC");

        /**
         * A {@code String} constant representing {@value #VIDEO_H265} media
         * type defined by RFC 7798.
         */
        public static final String VIDEO_H265 =
                "video/H265";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_H265} media
         * type defined by RFC 7798.
         */
        public static final MediaType VIDEO_H265_TYPE =
                new MediaType("video", "H265");

        /**
         * A {@code String} constant representing {@value #VIDEO_H266} media
         * type defined by RFC 9328.
         */
        public static final String VIDEO_H266 =
                "video/H266";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_H266} media
         * type defined by RFC 9328.
         */
        public static final MediaType VIDEO_H266_TYPE =
                new MediaType("video", "H266");

        /**
         * A {@code String} constant representing {@value #VIDEO_JPEG} media
         * type defined by RFC 3555.
         */
        public static final String VIDEO_JPEG =
                "video/JPEG";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_JPEG} media
         * type defined by RFC 3555.
         */
        public static final MediaType VIDEO_JPEG_TYPE =
                new MediaType("video", "JPEG");

        /**
         * A {@code String} constant representing {@value #VIDEO_JPEG2000} media
         * type defined by RFC 5371, and RFC 5372.
         */
        public static final String VIDEO_JPEG2000 =
                "video/jpeg2000";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_JPEG2000} media
         * type defined by RFC 5371, and RFC 5372.
         */
        public static final MediaType VIDEO_JPEG2000_TYPE =
                new MediaType("video", "jpeg2000");

        /**
         * A {@code String} constant representing {@value #VIDEO_JPEG2000_SCL} media
         * type defined by RFC 9828.
         */
        public static final String VIDEO_JPEG2000_SCL =
                "video/jpeg2000-scl";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_JPEG2000_SCL} media
         * type defined by RFC 9828.
         */
        public static final MediaType VIDEO_JPEG2000_SCL_TYPE =
                new MediaType("video", "jpeg2000-scl");

        /**
         * A {@code String} constant representing {@value #VIDEO_JXSV} media
         * type defined by RFC 9134.
         */
        public static final String VIDEO_JXSV =
                "video/jxsv";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_JXSV} media
         * type defined by RFC 9134.
         */
        public static final MediaType VIDEO_JXSV_TYPE =
                new MediaType("video", "jxsv");

        /**
         * A {@code String} constant representing {@value #VIDEO_MATROSKA} media
         * type defined by RFC 9559.
         */
        public static final String VIDEO_MATROSKA =
                "video/matroska";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_MATROSKA} media
         * type defined by RFC 9559.
         */
        public static final MediaType VIDEO_MATROSKA_TYPE =
                new MediaType("video", "matroska");

        /**
         * A {@code String} constant representing {@value #VIDEO_MATROSKA_3D} media
         * type defined by RFC 9559.
         */
        public static final String VIDEO_MATROSKA_3D =
                "video/matroska-3d";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_MATROSKA_3D} media
         * type defined by RFC 9559.
         */
        public static final MediaType VIDEO_MATROSKA_3D_TYPE =
                new MediaType("video", "matroska-3d");

        /**
         * A {@code String} constant representing {@value #VIDEO_MJ2} media
         * type defined by RFC 3745.
         */
        public static final String VIDEO_MJ2 =
                "video/mj2";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_MJ2} media
         * type defined by RFC 3745.
         */
        public static final MediaType VIDEO_MJ2_TYPE =
                new MediaType("video", "mj2");

        /**
         * A {@code String} constant representing {@value #VIDEO_MP1S} media
         * type defined by RFC 3555.
         */
        public static final String VIDEO_MP1S =
                "video/MP1S";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_MP1S} media
         * type defined by RFC 3555.
         */
        public static final MediaType VIDEO_MP1S_TYPE =
                new MediaType("video", "MP1S");

        /**
         * A {@code String} constant representing {@value #VIDEO_MP2P} media
         * type defined by RFC 3555.
         */
        public static final String VIDEO_MP2P =
                "video/MP2P";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_MP2P} media
         * type defined by RFC 3555.
         */
        public static final MediaType VIDEO_MP2P_TYPE =
                new MediaType("video", "MP2P");

        /**
         * A {@code String} constant representing {@value #VIDEO_MP2T} media
         * type defined by RFC 3555.
         */
        public static final String VIDEO_MP2T =
                "video/MP2T";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_MP2T} media
         * type defined by RFC 3555.
         */
        public static final MediaType VIDEO_MP2T_TYPE =
                new MediaType("video", "MP2T");

        /**
         * A {@code String} constant representing {@value #VIDEO_MP4} media
         * type defined by RFC 4337, and RFC 6381.
         */
        public static final String VIDEO_MP4 =
                "video/mp4";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_MP4} media
         * type defined by RFC 4337, and RFC 6381.
         */
        public static final MediaType VIDEO_MP4_TYPE =
                new MediaType("video", "mp4");

        /**
         * A {@code String} constant representing {@value #VIDEO_MP4V_ES} media
         * type defined by RFC 6416.
         */
        public static final String VIDEO_MP4V_ES =
                "video/MP4V-ES";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_MP4V_ES} media
         * type defined by RFC 6416.
         */
        public static final MediaType VIDEO_MP4V_ES_TYPE =
                new MediaType("video", "MP4V-ES");

        /**
         * A {@code String} constant representing {@value #VIDEO_MPV} media
         * type defined by RFC 3555.
         */
        public static final String VIDEO_MPV =
                "video/MPV";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_MPV} media
         * type defined by RFC 3555.
         */
        public static final MediaType VIDEO_MPV_TYPE =
                new MediaType("video", "MPV");

        /**
         * A {@code String} constant representing {@value #VIDEO_MPEG} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final String VIDEO_MPEG =
                "video/mpeg";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_MPEG} media
         * type defined by RFC 2045, and RFC 2046.
         */
        public static final MediaType VIDEO_MPEG_TYPE =
                new MediaType("video", "mpeg");

        /**
         * A {@code String} constant representing {@value #VIDEO_MPEG4_GENERIC} media
         * type defined by RFC 3640.
         */
        public static final String VIDEO_MPEG4_GENERIC =
                "video/mpeg4-generic";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_MPEG4_GENERIC} media
         * type defined by RFC 3640.
         */
        public static final MediaType VIDEO_MPEG4_GENERIC_TYPE =
                new MediaType("video", "mpeg4-generic");

        /**
         * A {@code String} constant representing {@value #VIDEO_NV} media
         * type defined by RFC 4856.
         */
        public static final String VIDEO_NV =
                "video/nv";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_NV} media
         * type defined by RFC 4856.
         */
        public static final MediaType VIDEO_NV_TYPE =
                new MediaType("video", "nv");

        /**
         * A {@code String} constant representing {@value #VIDEO_OGG} media
         * type defined by RFC 5334, and RFC 7845.
         */
        public static final String VIDEO_OGG =
                "video/ogg";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_OGG} media
         * type defined by RFC 5334, and RFC 7845.
         */
        public static final MediaType VIDEO_OGG_TYPE =
                new MediaType("video", "ogg");

        /**
         * A {@code String} constant representing {@value #VIDEO_PARITYFEC} media
         * type defined by RFC 3009.
         */
        public static final String VIDEO_PARITYFEC =
                "video/parityfec";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_PARITYFEC} media
         * type defined by RFC 3009.
         */
        public static final MediaType VIDEO_PARITYFEC_TYPE =
                new MediaType("video", "parityfec");

        /**
         * A {@code String} constant representing {@value #VIDEO_POINTER} media
         * type defined by RFC 2862.
         */
        public static final String VIDEO_POINTER =
                "video/pointer";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_POINTER} media
         * type defined by RFC 2862.
         */
        public static final MediaType VIDEO_POINTER_TYPE =
                new MediaType("video", "pointer");

        /**
         * A {@code String} constant representing {@value #VIDEO_QUICKTIME} media
         * type defined by RFC 6381.
         */
        public static final String VIDEO_QUICKTIME =
                "video/quicktime";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_QUICKTIME} media
         * type defined by RFC 6381.
         */
        public static final MediaType VIDEO_QUICKTIME_TYPE =
                new MediaType("video", "quicktime");

        /**
         * A {@code String} constant representing {@value #VIDEO_RAPTORFEC} media
         * type defined by RFC 6682.
         */
        public static final String VIDEO_RAPTORFEC =
                "video/raptorfec";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_RAPTORFEC} media
         * type defined by RFC 6682.
         */
        public static final MediaType VIDEO_RAPTORFEC_TYPE =
                new MediaType("video", "raptorfec");

        /**
         * A {@code String} constant representing {@value #VIDEO_RAW} media
         * type defined by RFC 4175.
         */
        public static final String VIDEO_RAW =
                "video/raw";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_RAW} media
         * type defined by RFC 4175.
         */
        public static final MediaType VIDEO_RAW_TYPE =
                new MediaType("video", "raw");

        /**
         * A {@code String} constant representing {@value #VIDEO_RTPLOOPBACK} media
         * type defined by RFC 6849.
         */
        public static final String VIDEO_RTPLOOPBACK =
                "video/rtploopback";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_RTPLOOPBACK} media
         * type defined by RFC 6849.
         */
        public static final MediaType VIDEO_RTPLOOPBACK_TYPE =
                new MediaType("video", "rtploopback");

        /**
         * A {@code String} constant representing {@value #VIDEO_RTX} media
         * type defined by RFC 4588.
         */
        public static final String VIDEO_RTX =
                "video/rtx";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_RTX} media
         * type defined by RFC 4588.
         */
        public static final MediaType VIDEO_RTX_TYPE =
                new MediaType("video", "rtx");

        /**
         * A {@code String} constant representing {@value #VIDEO_SCIP} media
         * type defined by RFC 9607.
         */
        public static final String VIDEO_SCIP =
                "video/scip";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_SCIP} media
         * type defined by RFC 9607.
         */
        public static final MediaType VIDEO_SCIP_TYPE =
                new MediaType("video", "scip");

        /**
         * A {@code String} constant representing {@value #VIDEO_SMPTE291} media
         * type defined by RFC 8331.
         */
        public static final String VIDEO_SMPTE291 =
                "video/smpte291";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_SMPTE291} media
         * type defined by RFC 8331.
         */
        public static final MediaType VIDEO_SMPTE291_TYPE =
                new MediaType("video", "smpte291");

        /**
         * A {@code String} constant representing {@value #VIDEO_SMPTE292M} media
         * type defined by RFC 3497.
         */
        public static final String VIDEO_SMPTE292M =
                "video/SMPTE292M";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_SMPTE292M} media
         * type defined by RFC 3497.
         */
        public static final MediaType VIDEO_SMPTE292M_TYPE =
                new MediaType("video", "SMPTE292M");

        /**
         * A {@code String} constant representing {@value #VIDEO_ULPFEC} media
         * type defined by RFC 5109.
         */
        public static final String VIDEO_ULPFEC =
                "video/ulpfec";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_ULPFEC} media
         * type defined by RFC 5109.
         */
        public static final MediaType VIDEO_ULPFEC_TYPE =
                new MediaType("video", "ulpfec");

        /**
         * A {@code String} constant representing {@value #VIDEO_VC1} media
         * type defined by RFC 4425.
         */
        public static final String VIDEO_VC1 =
                "video/vc1";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_VC1} media
         * type defined by RFC 4425.
         */
        public static final MediaType VIDEO_VC1_TYPE =
                new MediaType("video", "vc1");

        /**
         * A {@code String} constant representing {@value #VIDEO_VC2} media
         * type defined by RFC 8450.
         */
        public static final String VIDEO_VC2 =
                "video/vc2";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_VC2} media
         * type defined by RFC 8450.
         */
        public static final MediaType VIDEO_VC2_TYPE =
                new MediaType("video", "vc2");

        /**
         * A {@code String} constant representing {@value #VIDEO_VP8} media
         * type defined by RFC 7741.
         */
        public static final String VIDEO_VP8 =
                "video/VP8";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_VP8} media
         * type defined by RFC 7741.
         */
        public static final MediaType VIDEO_VP8_TYPE =
                new MediaType("video", "VP8");

        /**
         * A {@code String} constant representing {@value #VIDEO_VP9} media
         * type defined by RFC 9628.
         */
        public static final String VIDEO_VP9 =
                "video/VP9";

        /**
         * A {@link MediaType} constant representing {@value #VIDEO_VP9} media
         * type defined by RFC 9628.
         */
        public static final MediaType VIDEO_VP9_TYPE =
                new MediaType("video", "VP9");
    }
}
