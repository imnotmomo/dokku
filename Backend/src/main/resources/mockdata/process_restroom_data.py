#!/usr/bin/env python3
"""
NYC Public Restrooms Data Processor

This script processes the Public_Restrooms_20251005.csv file and saves
cleaned data to restrooms.json in the mockdata folder.

Usage: python process_restroom_data.py
"""

import csv
import json
import re
from typing import List, Dict, Optional, Any

def clean_string(text: str) -> str:
    """Clean and normalize string values"""
    if not text:
        return ""
    # Remove quotes, normalize whitespace
    cleaned = text.strip().replace('"', '').replace('\n', ' ').replace('\r', ' ')
    # Replace multiple spaces with single space
    cleaned = re.sub(r'\s+', ' ', cleaned)
    return cleaned

def normalize_hours(hours: str) -> Optional[str]:
    """Normalize hours format"""
    if not hours:
        return None
    
    hours = clean_string(hours)
    
    # Handle common patterns
    if 'CLOSED' in hours.upper():
        return "Closed"
    
    # Clean up common formatting issues
    hours = re.sub(r'\s+', ' ', hours)
    return hours if hours else None

def parse_amenities(accessibility: str, restroom_type: str, changing_stations: str) -> List[str]:
    """Parse amenities from CSV fields"""
    amenities = []
    
    if accessibility and accessibility.upper() not in ['N/A', '']:
        amenities.append(accessibility)
    
    if restroom_type and restroom_type.upper() not in ['N/A', '']:
        amenities.append(restroom_type)
    
    if changing_stations and changing_stations.upper() == 'YES':
        amenities.append('Changing Station')
    
    return amenities

def process_csv_to_restrooms(csv_path: str) -> List[Dict[str, Any]]:
    """Process CSV file and return list of cleaned restroom dictionaries"""
    restrooms = []
    
    with open(csv_path, 'r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        
        for row_num, row in enumerate(reader, start=2):
            try:
                # Extract fields
                facility_name = clean_string(row.get('Facility Name', ''))
                status = clean_string(row.get('Status', ''))
                hours = normalize_hours(row.get('Hours of Operation', ''))
                amenities = clean_string(row.get('Accessibility', ''))
                latitude_str = clean_string(row.get('Latitude', ''))
                longitude_str = clean_string(row.get('Longitude', ''))
                location = clean_string(row.get('Location', ''))
                
                # Skip non-operational restrooms
                if status.upper() != 'OPERATIONAL':
                    continue
                
                # Skip if missing essential data
                if not facility_name or not latitude_str or not longitude_str:
                    continue
                
                # Parse coordinates
                try:
                    latitude = float(latitude_str)
                    longitude = float(longitude_str)
                except ValueError:
                    print(f"Row {row_num}: Invalid coordinates - skipping")
                    continue
                
                # Parse amenities
                amenities = parse_amenities(accessibility, restroom_type, changing_stations)
                
                # Create restroom object matching your Restroom model
                restroom = {
                    'id': len(restrooms) + 1,  # Simple auto-increment
                    'name': facility_name,
                    'latitude': latitude,
                    'longitude': longitude,
                    'address': location if location else None,
                    'hours': hours,
                    'amenities': amenities,
                    'avgRating': 0.0,
                    'visitCount': 0,
                    'pendingEdits': []
                }
                
                restrooms.append(restroom)
                
            except Exception as e:
                print(f"Error processing row {row_num}: {e}")
                continue
    
    return restrooms

def main():
    """Main processing function"""
    csv_path = "Public_Restrooms_20251005.csv"
    json_output_path = "restrooms.json"
    
    print("Processing NYC Public Restrooms CSV...")
    print(f"Input file: {csv_path}")
    
    # Process CSV
    restrooms = process_csv_to_restrooms(csv_path)
    
    print(f"Successfully processed {len(restrooms)} operational restrooms")
    
    # Save JSON file
    with open(json_output_path, 'w', encoding='utf-8') as file:
        json.dump(restrooms, file, indent=2, ensure_ascii=False)
    
    print(f"JSON file saved: {json_output_path}")
    print(f"Total restrooms: {len(restrooms)}")

if __name__ == "__main__":
    main()