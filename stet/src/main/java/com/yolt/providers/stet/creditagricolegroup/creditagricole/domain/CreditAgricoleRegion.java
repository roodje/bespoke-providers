package com.yolt.providers.stet.creditagricolegroup.creditagricole.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CreditAgricoleRegion {
    CAM_ALPES_PROVENCE("Alpes Provence"),
    CAM_ALSACE_VOSGES("Alsace-Vosges"),
    CAM_ANJOU_MAINE("Anjou Maine"),
    CAM_AQUITAINE("Aquitaine"),
    CAM_ATLANTIQUE_VENDEE("Atlantique Vendée"),
    CAM_BRIE_PICARDIE("Brie Picardie"),
    CAM_CENTRE_FRANCE("Centre France"),
    CAM_CENTRE_LOIRE("Centre Loire"),
    CAM_CENTRE_OUEST("Centre Ouest"),
    CAM_CENTRE_EST("Centre Est"),
    CAM_CHAMPAGNE_BOURGOGNE("Champagne Bourgogne"),
    CAM_CHARENTE_MARITIME_DEUX_SEVRES("Charente-Maritime Deux-Sèvres"),
    CAM_CHARENTE_PERIGORD("Charente Périgord"),
    CAM_CORSE("Corse"),
    CAM_COTES_DARMOR("Côtes d'Armor"),
    CAM_DES_SAVOIE("Des Savoie"),
    CAM_FINISTERE("Finistere"),
    CAM_FRANCHE_COMTE("Franche-Comté"),
    CAM_GUADELOUPE("Guadeloupe"),
    CAM_ILE_DE_FRANCE("Ile-de-France"),
    CAM_ILLE_ET_VILAINE("Ille-et-Vilaine"),
    CAM_LANGUEDOC("Languedoc"),
    CAM_LOIRE_HAUTE_LOIRE("Loire Haute Loire"),
    CAM_LORRAINE("Lorraine"),
    CAM_MARTINIQUE_GUYANE("Martinique Guyane"),
    CAM_MORBIHAN("Morbihan"),
    CAM_NORD_DE_FRANCE("Nord de France"),
    CAM_NORD_EST("Nord Est"),
    CAM_NORD_MIDI_PYRENEES("Nord Midi-Pyrénées"),
    CAM_NORMANDIE("Normandie"),
    CAM_NORMANDIE_SEINE("Normandie Seine"),
    CAM_PROVENCE_COTE_DAZUR("Provence Côte d'Azur"),
    CAM_PYRENEES_GASCOGNE("Pyrénées Gascogne"),
    CAM_REUNION("Réunion"),
    CAM_SUD_MEDITERRANEE("Sud Méditerranée"),
    CAM_SUD_RHONE_ALPES("Sud Rhône Alpes"),
    CAM_TOULOUSE_31("Toulouse 31"),
    CAM_TOURRAINE_POITOU("Tourraine Poitou"),
    CAM_VAL_DE_FRANCE("Val de France"),
    CAM_BANQUE_CHALUS("Banque Chalus");

    private final String displayName;
}