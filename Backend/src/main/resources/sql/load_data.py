#!/usr/bin/env python3
"""
Load restrooms.json data into PostgreSQL database.
"""

import json
import psycopg2
from psycopg2.extras import execute_values
import sys
from pathlib import Path
import getpass

# Database connection parameters
DB_PARAMS = {
    'dbname': 'restroom_finder',
    'user': 'zhimeiwang',
    'host': 'localhost',
    'port': 5432,
    'password': getpass.getpass('Database password: ') 
}

def create_tables(cursor):
    """Create database tables using schema.sql"""
    try:
        # Read schema.sql
        schema_path = Path('./schema.sql')
        with open(schema_path, 'r') as f:
            schema_sql = f.read()
        
        # Execute schema
        cursor.execute(schema_sql)
        print("Tables created successfully")
        
    except Exception as e:
        print(f"Error creating tables: {e}")
        sys.exit(1)

def load_json_data():
    """Load data from restrooms.json"""
    try:
        json_path = Path('../mockdata/restrooms.json')
        with open(json_path, 'r') as f:
            data = json.load(f)
        print(f"Loaded {len(data)} restrooms from JSON")
        return data
    except Exception as e:
        print(f"Error loading JSON data: {e}")
        sys.exit(1)

def insert_restrooms(cursor, restrooms):
    """Insert restrooms into database"""
    try:
        # Prepare data for insertion
        values = []
        for restroom in restrooms:
            values.append((
                restroom['id'],
                restroom['name'],
                restroom['latitude'],
                restroom['longitude'],
                restroom.get('address'),
                restroom.get('hours'),
                restroom.get('amenities', []),  # Pass amenities as a list
                restroom.get('avgRating', 0.0),
                restroom.get('visitCount', 0)
            ))

        # Insert data using execute_values
        insert_sql = """
            INSERT INTO restroom (
                id, name, latitude, longitude, address,
                hours, amenities, avg_rating, visit_count
            ) VALUES %s
        """
        execute_values(cursor, insert_sql, values)
        print(f"Inserted {len(values)} restrooms into database")
        
    except Exception as e:
        print(f"Error inserting data: {e}")
        sys.exit(1)

def insert_users(cursor):
    """Insert users into database"""
    try:
        # Prepare data for insertion
        users = [ ["zw3099@columbia.edu", "password", "ADMIN", "123", "123"] ]


        # Insert data using execute_values
        insert_sql = """
            INSERT INTO users (username, password, role, token, refresh_token
            ) VALUES %s
        """
        execute_values(cursor, insert_sql, users)
        print(f"Inserted {len(users)} users into database")
        
    except Exception as e:
        print(f"Error inserting data: {e}")
        sys.exit(1)

def main():
    """Main function"""
    print("Starting data load process...")
    print(f"Database: {DB_PARAMS['dbname']} on {DB_PARAMS['host']}")
    
    try:
        # Connect to database
        conn = psycopg2.connect(**DB_PARAMS)
        conn.autocommit = False
        cursor = conn.cursor()
        
        # Create tables
        create_tables(cursor)
        
        # Load JSON data
        restrooms = load_json_data()
        
        # Insert data
        insert_restrooms(cursor, restrooms)
        insert_users(cursor)
        
        # Commit transaction
        conn.commit()
        print("Data load completed successfully!")
        
    except Exception as e:
        print(f"Error: {e}")
        if 'conn' in locals():
            conn.rollback()
        sys.exit(1)
        
    finally:
        if 'cursor' in locals():
            cursor.close()
        if 'conn' in locals():
            conn.close()

if __name__ == "__main__":
    main()