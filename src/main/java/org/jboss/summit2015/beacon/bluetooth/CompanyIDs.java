package org.jboss.summit2015.beacon.bluetooth;
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http:://www.apache.org/licenses/LICENSE_2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * The assigned company ids as parsed from
 * https://www.bluetooth.org/en-us/specification/assigned-numbers/company-identifiers
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public enum CompanyIDs {
   Ericsson_Technology_Licensing(0x0000),
  	Nokia_Mobile_Phones(0x0001),
  	Intel_Corp(0x0002),
  	IBM_Corp(0x0003),
  	Toshiba_Corp(0x0004),
  	_3Com(0x0005),
  	Microsoft(0x0006),
  	Lucent(0x0007),
  	Motorola(0x0008),
  	Infineon_Technologies_AG(0x0009),
  	Cambridge_Silicon_Radio(0x000A),
  	Silicon_Wave(0x000B),
  	Digianswer_A_S(0x000C),
  	Texas_Instruments_Inc(0x000D),
  	Ceva_Inc_(0x000E),
  	Broadcom_Corporation(0x000F),
  	Mitel_Semiconductor(0x0010),
  	Widcomm_Inc(0x0011),
  	Zeevo_Inc(0x0012),
  	Atmel_Corporation(0x0013),
  	Mitsubishi_Electric_Corporation(0x0014),
  	RTX_Telecom_A_S(0x0015),
  	KC_Technology_Inc(0x0016),
  	NewLogic(0x0017),
  	Transilica_Inc(0x0018),
  	Rohde_Schwarz_GmbH_Co_KG(0x0019),
  	TTPCom_Limited(0x001A),
  	Signia_Technologies_Inc(0x001B),
  	Conexant_Systems_Inc(0x001C),
  	Qualcomm(0x001D),
  	Inventel(0x001E),
  	AVM_Berlin(0x001F),
  	BandSpeed_Inc(0x0020),
  	Mansella_Ltd(0x0021),
  	NEC_Corporation(0x0022),
  	WavePlus_Technology_Co_Ltd(0x0023),
  	Alcatel(0x0024),
  	NXP_Semiconductors(0x0025),
  	C_Technologies(0x0026),
  	Open_Interface(0x0027),
  	R_F_Micro_Devices(0x0028),
  	Hitachi_Ltd(0x0029),
  	Symbol_Technologies_Inc(0x002A),
  	Tenovis(0x002B),
  	Macronix_International_Co_Ltd(0x002C),
  	GCT_Semiconductor(0x002D),
  	Norwood_Systems(0x002E),
  	MewTel_Technology_Inc(0x002F),
  	ST_Microelectronics(0x0030),
  	Synopsis(0x0031),
  	Red_M_Communications_Ltd(0x0032),
  	Commil_Ltd(0x0033),
  	Computer_Access_Technology_Corporation(0x0034),
  	Eclipse_HQ_Espana_SL(0x0035),
  	Renesas_Electronics_Corporation(0x0036),
  	Mobilian_Corporation(0x0037),
  	Terax(0x0038),
  	Integrated_System_Solution_Corp(0x0039),
  	Matsushita_Electric_Industrial_Co_Ltd(0x003A),
  	Gennum_Corporation(0x003B),
  	BlackBerry_Limited_(0x003C),
  	IPextreme_Inc(0x003D),
  	Systems_and_Chips_Inc(0x003E),
  	Bluetooth_SIG_Inc(0x003F),
  	Seiko_Epson_Corporation(0x0040),
  	Integrated_Silicon_Solution_Taiwan_Inc(0x0041),
  	CONWISE_Technology_Corporation_Ltd(0x0042),
  	PARROT_SA(0x0043),
  	Socket_Mobile(0x0044),
  	Atheros_Communications_Inc(0x0045),
  	MediaTek_Inc(0x0046),
  	Bluegiga(0x0047),
  	Marvell_Technology_Group_Ltd(0x0048),
  	_3DSP_Corporation(0x0049),
  	Accel_Semiconductor_Ltd(0x004A),
  	Continental_Automotive_Systems(0x004B),
  	Apple_Inc(0x004C),
  	Staccato_Communications_Inc(0x004D),
  	Avago_Technologies(0x004E),
  	APT_Licensing_Ltd(0x004F),
  	SiRF_Technology(0x0050),
  	Tzero_Technologies_Inc(0x0051),
  	J_M_Corporation(0x0052),
  	Free2move_AB(0x0053),
  	_3DiJoy_Corporation(0x0054),
  	Plantronics_Inc(0x0055),
  	Sony_Ericsson_Mobile_Communications(0x0056),
  	Harman_International_Industries_Inc(0x0057),
  	Vizio_Inc(0x0058),
  	Nordic_Semiconductor_ASA(0x0059),
  	EM_Microelectronic_Marin_SA(0x005A),
  	Ralink_Technology_Corporation(0x005B),
  	Belkin_International_Inc(0x005C),
  	Realtek_Semiconductor_Corporation(0x005D),
  	Stonestreet_One_LLC(0x005E),
  	Wicentric_Inc(0x005F),
  	RivieraWaves_SAS(0x0060),
  	RDA_Microelectronics(0x0061),
  	Gibson_Guitars(0x0062),
  	MiCommand_Inc(0x0063),
  	Band_XI_International_LLC(0x0064),
  	Hewlett_Packard_Company(0x0065),
  	_9Solutions_Oy(0x0066),
  	GN_Netcom_A_S(0x0067),
  	General_Motors(0x0068),
  	A_D_Engineering_Inc(0x0069),
  	MindTree_Ltd(0x006A),
  	Polar_Electro_OY(0x006B),
  	Beautiful_Enterprise_Co_Ltd(0x006C),
  	BriarTek_Inc(0x006D),
  	Summit_Data_Communications_Inc(0x006E),
  	Sound_ID(0x006F),
  	Monster_LLC(0x0070),
  	connectBlue_AB(0x0071),
  	ShangHai_Super_Smart_Electronics_Co_Ltd(0x0072),
  	Group_Sense_Ltd(0x0073),
  	Zomm_LLC(0x0074),
  	Samsung_Electronics_Co_Ltd(0x0075),
  	Creative_Technology_Ltd(0x0076),
  	Laird_Technologies(0x0077),
  	Nike_Inc(0x0078),
  	lesswire_AG(0x0079),
  	MStar_Semiconductor_Inc(0x007A),
  	Hanlynn_Technologies(0x007B),
  	A_R_Cambridge(0x007C),
  	Seers_Technology_Co_Ltd(0x007D),
  	Sports_Tracking_Technologies_Ltd(0x007E),
  	Autonet_Mobile(0x007F),
  	DeLorme_Publishing_Company_Inc(0x0080),
  	WuXi_Vimicro(0x0081),
  	Sennheiser_Communications_A_S(0x0082),
  	TimeKeeping_Systems_Inc(0x0083),
  	Ludus_Helsinki_Ltd(0x0084),
  	BlueRadios_Inc(0x0085),
  	equinox_AG(0x0086),
  	Garmin_International_Inc(0x0087),
  	Ecotest(0x0088),
  	GN_ReSound_A_S(0x0089),
  	Jawbone(0x008A),
  	Topcorn_Positioning_Systems_LLC(0x008B),
  	Gimbal_Inc(0x008C),
  	Zscan_Software(0x008D),
  	Quintic_Corp(0x008E),
  	Stollman_E_V_GmbH(0x008F),
  	Funai_Electric_Co_Ltd(0x0090),
  	Advanced_PANMOBIL_Systems_GmbH_Co_KG(0x0091),
  	ThinkOptics_Inc(0x0092),
  	Universal_Electronics_Inc(0x0093),
  	Airoha_Technology_Corp(0x0094),
  	NEC_Lighting_Ltd(0x0095),
  	ODM_Technology_Inc(0x0096),
  	ConnecteDevice_Ltd(0x0097),
  	zer01tv_GmbH(0x0098),
  	iTech_Dynamic_Global_Distribution_Ltd(0x0099),
  	Alpwise(0x009A),
  	Jiangsu_Toppower_Automotive_Electronics_Co_Ltd(0x009B),
  	Colorfy_Inc(0x009C),
  	Geoforce_Inc(0x009D),
  	Bose_Corporation(0x009E),
  	Suunto_Oy(0x009F),
  	Kensington_Computer_Products_Group(0x00A0),
  	SR_Medizinelektronik(0x00A1),
  	Vertu_Corporation_Limited(0x00A2),
  	Meta_Watch_Ltd(0x00A3),
  	LINAK_A_S(0x00A4),
  	OTL_Dynamics_LLC(0x00A5),
  	Panda_Ocean_Inc(0x00A6),
  	Visteon_Corporation(0x00A7),
  	ARP_Devices_Limited(0x00A8),
  	Magneti_Marelli_SpA(0x00A9),
  	CAEN_RFID_srl(0x00AA),
  	Ingenieur_Systemgruppe_Zahn_GmbH(0x00AB),
  	Green_Throttle_Games(0x00AC),
  	Peter_Systemtechnik_GmbH(0x00AD),
  	Omegawave_Oy(0x00AE),
  	Cinetix(0x00AF),
  	Passif_Semiconductor_Corp(0x00B0),
  	Saris_Cycling_Group_Inc(0x00B1),
	Bekey_A_S(0x00B2),
	Clarinox_Technologies_Pty_Ltd(0x00B3),
  	BDE_Technology_Co_Ltd(0x00B4),
  	Swirl_Networks(0x00B5),
  	Meso_international(0x00B6),
  	TreLab_Ltd(0x00B7),
  	Qualcomm_Innovation_Center_Inc(0x00B8),
  	Johnson_Controls_Inc(0x00B9),
  	Starkey_Laboratories_Inc(0x00BA),
  	S_Power_Electronics_Limited(0x00BB),
  	Ace_Sensor_Inc(0x00BC),
  	Aplix_Corporation(0x00BD),
  	AAMP_of_America(0x00BE),
  	Stalmart_Technology_Limited(0x00BF),
  	AMICCOM_Electronics_Corporation(0x00C0),
  	Shenzhen_Excelsecu_Data_Technology_Co_Ltd(0x00C1),
  	Geneq_Inc(0x00C2),
  	adidas_AG(0x00C3),
  	LG_Electronics(0x00C4),
  	Onset_Computer_Corporation(0x00C5),
  	Selfly_BV(0x00C6),
  	Quuppa_Oy(0x00C7),
  	GeLo_Inc(0x00C8),
  	Evluma(0x00C9),
  	MC10(0x00CA),
  	Binauric_SE(0x00CB),
  	Beats_Electronics(0x00CC),
  	Microchip_Technology_Inc(0x00CD),
  	Elgato_Systems_GmbH(0x00CE),
  	ARCHOS_SA(0x00CF),
  	Dexcom_Inc(0x00D0),
  	Polar_Electro_Europe_BV(0x00D1),
  	Dialog_Semiconductor_BV(0x00D2),
  	Taixingbang_Technology_Co_LTD(0x00D3),
  	Kawantech(0x00D4),
  	Austco_Communication_Systems(0x00D5),
  	Timex_Group_USA_Inc(0x00D6),
  	Qualcomm_Technologies_Inc(0x00D7),
  	Qualcomm_Connected_Experiences_Inc(0x00D8),
  	Voyetra_Turtle_Beach(0x00D9),
  	txtr_GmbH(0x00DA),
  	Biosentronics(0x00DB),
  	Procter___Gamble(0x00DC),
  	Hosiden_Corporation(0x00DD),
  	Muzik_LLC(0x00DE),
  	Misfit_Wearables_Corp(0x00DF),
  	Google(0x00E0),
  	Danlers_Ltd(0x00E1),
  	Semilink_Inc(0x00E2),
  	inMusic_Brands_Inc(0x00E3),
  	LS_Research_Inc(0x00E4),
  	Eden_Software_Consultants_Ltd(0x00E5),
  	Freshtemp(0x00E6),
  	KS_Technologies(0x00E7),
  	ACTS_Technologies(0x00E8),
  	Vtrack_Systems(0x00E9),
  	Nielsen_Kellerman_Company(0x00EA),
  	Server_Technology_Inc(0x00EB),
  	BioResearch_Associates(0x00EC),
  	Jolly_Logic_LLC(0x00ED),
  	Above_Average_Outcomes_Inc(0x00EE),
  	Bitsplitters_GmbH(0x00EF),
  	PayPal_Inc(0x00F0),
  	Witron_Technology_Limited(0x00F1),
  	Aether_Things_Inc(0x00F2),
  	Kent_Displays_Inc(0x00F3),
  	Nautilus_Inc(0x00F4),
  	Smartifier_Oy(0x00F5),
  	Elcometer_Limited(0x00F6),
  	VSN_Technologies_Inc(0x00F7),
  	AceUni_Corp_Ltd(0x00F8),
  	StickNFind(0x00F9),
  	Crystal_Code_AB(0x00FA),
  	KOUKAAM_as(0x00FB),
  	Delphi_Corporation(0x00FC),
  	ValenceTech_Limited(0x00FD),
  	Reserved(0x00FE),
  	Typo_Products_LLC(0x00FF),
  	TomTom_International_BV(0x0100),
  	Fugoo_Inc(0x0101),
  	Keiser_Corporation(0x0102),
  	Bang___Olufsen_A_S(0x0103),
  	PLUS_Locations_Systems_Pty_Ltd(0x0104),
  	Ubiquitous_Computing_Technology_Corporation(0x0105),
  	Innovative_Yachtter_Solutions(0x0106),
  	William_Demant_Holding_A_S(0x0107),
  	Chicony_Electronics_Co_Ltd(0x0108),
  	Atus_BV(0x0109),
  	Codegate_Ltd(0x010A),
  	ERi_Inc(0x010B),
  	Transducers_Direct_LLC(0x010C),
  	Fujitsu_Ten_Limited(0x010D),
  	Audi_AG(0x010E),
  	HiSilicon_Technologies_Co_Ltd(0x010F),
  	Nippon_Seiki_Co_Ltd(0x0110),
  	Steelseries_ApS(0x0111),
  	vyzybl_Inc(0x0112),
  	Openbrain_Technologies_Co_Ltd(0x0113),
  	Xensr(0x0114),
  	esolutions(0x0115),
  	_1OAK_Technologies(0x0116),
  	Wimoto_Technologies_Inc(0x0117),
  	Radius_Networks_Inc(0x0118),
  	Wize_Technology_Co_Ltd(0x0119),
  	Qualcomm_Labs_Inc(0x011A),
  	Aruba_Networks(0x011B),
  	Baidu(0x011C),
  	Arendi_AG(0x011D),
  	Skoda_Auto_as(0x011E),
  	Volkswagon_AG(0x011F),
  	Porsche_AG(0x0120),
  	Sino_Wealth_Electronic_Ltd(0x0121),
  	AirTurn_Inc(0x0122),
  	Kinsa_Inc(0x0123),
  	HID_Global(0x0124),
  	SEAT_es(0x0125),
  	Promethean_Ltd(0x0126),
  	Salutica_Allied_Solutions(0x0127),
  	GPSI_Group_Pty_Ltd(0x0128),
  	Nimble_Devices_Oy(0x0129),
  	Changzhou_Yongse_Infotech_Co_Ltd(0x012A),
  	SportIQ(0x012B),
  	TEMEC_Instruments_BV(0x012C),
  	Sony_Corporation(0x012D),
  	ASSA_ABLOY(0x012E),
  	Clarion_Co_Ltd(0x012F),
  	Warehouse_Innovations(0x0130),
  	Cypress_Semiconductor_Corporation(0x0131),
  	MADS_Inc(0x0132),
  	Blue_Maestro_Limited(0x0133),
  	Resolution_Products_Inc(0x0134),
  	Airewear_LLC(0x0135),
  	Seed_Labs_Inc(0x0136),
  	Prestigio_Plaza_Ltd(0x0137),
  	NTEO_Inc(0x0138),
  	Focus_Systems_Corporation(0x0139),
  	Tencent_Holdings_Limited(0x013A),
  	Allegion(0x013B),
  	Murata_Manufacuring_Co_Ltd(0x013C),
  	Nod_Inc(0x013E),
  	B_B_Manufacturing_Company(0x013F),
  	Alpine_Electronics_Co_Ltd(0x0140),
  	FedEx_Services(0x0141),
  	Grape_Systems_Inc(0x0142),
  	Bkon_Connect(0x0143),
  	Lintech_GmbH(0x0144),
  	Novatel_Wireless(0x0145),
  	Ciright(0x0146),
  	Mighty_Cast_Inc(0x0147),
  	Ambimat_Electronics(0x0148),
  	Perytons_Ltd(0x0149),
  	Tivoli_Audio_LLC(0x014A),
  	Master_Lock(0x014B),
  	Mesh_Net_Ltd(0x014C),
  	Huizhou_Desay_SV_Automotive_CO_LTD(0x014D),
  	Tangerine_Inc(0x014E),
  	B_W_Group_Ltd(0x014F),
  	Pioneer_Corporation(0x0150),
  	OnBeep(0x0151),
  	Vernier_Software_Technology(0x0152),
  	ROL_Ergo(0x0153),
  	Pebble_Technology(0x0154),
  	NETATMO(0x0155),
  	Accumulate_AB(0x0156),
  	Anhui_Huami_Information_Technology_Co_Ltd(0x0157),
  	Inmite_sro(0x0158),
  	ChefSteps_Inc(0x0159),
  	micas_AG(0x015A),
  	Biomedical_Research_Ltd(0x015B),
  	Pitius_Tec_SL(0x015C),
  	Estimote_Inc(0x015D),
  	Unikey_Technologies_Inc(0x015E),
  	Timer_Cap_Co(0x015F),
  	AwoX(0x0160),
  	yikes(0x0161),
  	MADSGlobal_NZ_Ltd(0x0162),
  	PCH_International(0x0163),
  	Qingdao_Yeelink_Information_Technology_Co_Ltd(0x0164),
  	Milwaukee_Tool(0x0165),
  	MISHIK_Pte_Ltd(0x0166),
  	Bayer_HealthCare(0x0167),
  	Spicebox_LLC(0x0168),
  	emberlight(0x0169),
  	Cooper_Atkins_Corporation(0x016A),
  	Qblinks(0x016B),
  	MYSPHERA(0x016C),
  	LifeScan_Inc(0x016D),
  	Volantic_AB(0x016E),
  	Podo_Labs_Inc(0x016F),
  	Roche_Diabetes_Care_AG(0x0170),
  	Amazon_Fulfillment_Service(0x0171),
  	Connovate_Technology_Private_Limited(0x0172),
  	Kocomojo_LLC(0x0173),
  	Everykey_LLC(0x0174),
  	Dynamic_Controls(0x0175),
  	SentriLock(0x0176),
  	I_SYST_inc(0x0177),
  	CASIO_COMPUTER_CO_LTD(0x0178),
  	LAPIS_Semiconductor_Co_Ltd(0x0179),
  	Telemonitor_Inc(0x017A),
  	taskit_GmbH(0x017B),
  	Daimler_AG(0x017C),
  	BatAndCat(0x017D),
  	BluDotz_Ltd(0x017E),
  	XTel_ApS(0x017F),
  	Gigaset_Communications_GmbH(0x0180),
  	Gecko_Health_Innovations_Inc(0x0181),
  	HOP_Ubiquitous(0x0182),
  	To_Be_Assigned(0x0183),
  	Nectar(0x0184),
  	belapps_LLC(0x0185),
  	CORE_Lighting_Ltd(0x0186),
  	Seraphim_Sense_Ltd(0x0187),
  	Unico_RBC(0x0188),
  	Physical_Enterprises_Inc(0x0189),
  	Able_Trend_Technology_Limited(0x018A),
  	Konica_Minolta_Inc(0x018B),
  	Wilo_SE(0x018C),
  	Extron_Design_Services(0x018D),
  	Fitbit_Inc(0x018E),
  	Fireflies_Systems(0x018F),
  	Intelletto_Technologies_Inc(0x0190),
  	FDK_CORPORATION(0x0191),
  	Cloudleaf_Inc(0x0192),
  	Maveric_Automation_LLC(0x0193),
  	Acoustic_Stream_Corporation(0x0194),
  	Zuli(0x0195),
  	Paxton_Access_Ltd(0x0196),
  	WiSilica_Inc(0x0197),
  	Vengit_Limited(0x0198),
  	SALTO_SYSTEMS_SL(0x0199),
  	T_Engine_Forum(0x019A),
  	CUBETECH_sro(0x019B),
  	Cokiya_Incorporated(0x019C),
  	CVS_Health(0x019D),
  	Ceruus(0x019E),
  	Strainstall_Ltd(0x019F),
  	Channel_Enterprises_Ltd(0x01A0),
  	FIAMM(0x01A1),
  	GIGALANECO_LTD(0x01A2),
  	EROAD(0x01A3),
  	Mine_Safety_Appliances(0x01A4),
  	Icon_Health_and_Fitness(0x01A5),
  	Asandoo_GmbH(0x01A6),
  	ENERGOUS_CORPORATION(0x01A7),
  	Taobao(0x01A8),
  	Canon_Inc(0x01A9),
  	Geophysical_Technology_Inc(0x01AA),
  	Facebook_Inc(0x01AB),
  	Nipro_Diagnostics_Inc(0x01AC),
  	FlightSafety_International(0x01AD),
  	Earlens_Corporation(0x01AE),
  	Sunrise_Micro_Devices_Inc(0x01AF),
  	Star_Micronics_Co_Ltd(0x01B0),
  	Netizens_Sp_z_oo(0x01B1),
   ;

   private CompanyIDs(int id) {
      this.id = id;
   }
   private int id;
   private String name;
}
