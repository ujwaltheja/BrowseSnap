import plotly.graph_objects as go
import plotly.express as px
import json

# Load the data
data = {
    "components": [
        {"category": "Mobile App Architecture", "name": "SearchModule", "status_percent": 0, "estimated_hours": 16, "priority": "HIGH", "dependencies": 0},
        {"category": "Mobile App Architecture", "name": "CommandSender", "status_percent": 0, "estimated_hours": 20, "priority": "HIGH", "dependencies": 0},
        {"category": "Mobile App Architecture", "name": "PairingModule (Mobile)", "status_percent": 0, "estimated_hours": 24, "priority": "MEDIUM", "dependencies": 0},
        {"category": "Mobile App Architecture", "name": "HistoryModule", "status_percent": 0, "estimated_hours": 12, "priority": "MEDIUM", "dependencies": 0},
        {"category": "Mobile App Architecture", "name": "Mobile UI", "status_percent": 0, "estimated_hours": 32, "priority": "HIGH", "dependencies": 2},
        {"category": "TV App Architecture", "name": "WebSocketServer", "status_percent": 0, "estimated_hours": 20, "priority": "HIGH", "dependencies": 0},
        {"category": "TV App Architecture", "name": "CommandHandler", "status_percent": 0, "estimated_hours": 24, "priority": "HIGH", "dependencies": 1},
        {"category": "TV App Architecture", "name": "WebViewModule (TV)", "status_percent": 0, "estimated_hours": 12, "priority": "HIGH", "dependencies": 1},
        {"category": "TV App Architecture", "name": "VideoPlayerModule", "status_percent": 0, "estimated_hours": 28, "priority": "HIGH", "dependencies": 1},
        {"category": "TV App Architecture", "name": "PairingModule (TV)", "status_percent": 0, "estimated_hours": 20, "priority": "MEDIUM", "dependencies": 0},
        {"category": "TV App Architecture", "name": "TV UI", "status_percent": 0, "estimated_hours": 36, "priority": "HIGH", "dependencies": 2},
        {"category": "Communication Layer", "name": "WebSocket Protocol", "status_percent": 0, "estimated_hours": 8, "priority": "CRITICAL", "dependencies": 0}
    ]
}

components = data["components"]

# Organize data by category in reverse order for proper display
categories = ["Mobile App Architecture", "TV App Architecture", "Communication Layer"]
y_labels = []
hours_values = []
priority_labels = []
hover_texts = []
category_labels = []

for category in categories:
    category_components = [c for c in components if c["category"] == category]
    # Sort by priority (CRITICAL first, then HIGH, then MEDIUM) and then by hours
    priority_order = {'CRITICAL': 0, 'HIGH': 1, 'MEDIUM': 2}
    category_components.sort(key=lambda x: (priority_order[x['priority']], -x['estimated_hours']))
    
    for comp in category_components:
        # Create clean component name with priority badge
        priority_badge = f"[{comp['priority']}]"
        y_labels.append(f"{comp['name']} {priority_badge}")
        hours_values.append(comp['estimated_hours'])
        priority_labels.append(comp['priority'])
        category_labels.append(category)
        
        hover_texts.append(f"Component: {comp['name']}<br>"
                          f"Category: {comp['category']}<br>"
                          f"Status: Not Started (0%)<br>"
                          f"Est Hours: {comp['estimated_hours']}h<br>"
                          f"Priority: {comp['priority']}<br>"
                          f"Dependencies: {comp['dependencies']}")

# Create the figure
fig = go.Figure()

# Add main bars - Red for "Not Started" status as per instructions
fig.add_trace(go.Bar(
    y=y_labels,
    x=hours_values,
    orientation='h',
    marker=dict(
        color='#DB4545',  # Red for "Not Started"
        line=dict(color='rgba(0,0,0,0.3)', width=1)
    ),
    text=[f"{h}h" for h in hours_values],
    textposition='inside',
    textfont=dict(color='white', size=12, family='Arial Black'),
    hovertemplate='%{hovertext}<extra></extra>',
    hovertext=hover_texts,
    name='Not Started (0%)'
))

# Update layout
fig.update_layout(
    title='BrowseSnap Component Matrix',
    xaxis_title='Est Hours',
    yaxis_title='Components by Category',
    showlegend=False,
    yaxis=dict(
        categoryorder='array',
        categoryarray=y_labels,
        showgrid=False
    ),
    xaxis=dict(
        range=[0, max(hours_values) * 1.1],
        showgrid=True,
        gridcolor='rgba(0,0,0,0.1)'
    ),
    plot_bgcolor='white'
)

# Add category separators and labels
mobile_components = [i for i, cat in enumerate(category_labels) if cat == "Mobile App Architecture"]
tv_components = [i for i, cat in enumerate(category_labels) if cat == "TV App Architecture"] 
comm_components = [i for i, cat in enumerate(category_labels) if cat == "Communication Layer"]

# Add horizontal lines to separate categories
if tv_components:
    fig.add_hline(y=max(tv_components) + 0.5, line_dash="solid", line_color="gray", line_width=2)
if mobile_components:
    fig.add_hline(y=max(mobile_components) + 0.5, line_dash="solid", line_color="gray", line_width=2)

# Add category labels as text annotations on the left
max_hours = max(hours_values)

# Mobile App category label
if mobile_components:
    mobile_mid = (min(mobile_components) + max(mobile_components)) / 2
    fig.add_trace(go.Scatter(
        x=[-max_hours * 0.15],
        y=[mobile_mid],
        mode='text',
        text='Mobile App',
        textfont=dict(size=14, color='#1FB8CD', family='Arial Black'),
        showlegend=False,
        hoverinfo='skip'
    ))

# TV App category label  
if tv_components:
    tv_mid = (min(tv_components) + max(tv_components)) / 2
    fig.add_trace(go.Scatter(
        x=[-max_hours * 0.15],
        y=[tv_mid],
        mode='text',
        text='TV App',
        textfont=dict(size=14, color='#2E8B57', family='Arial Black'),
        showlegend=False,
        hoverinfo='skip'
    ))

# Communication category label
if comm_components:
    comm_mid = (min(comm_components) + max(comm_components)) / 2
    fig.add_trace(go.Scatter(
        x=[-max_hours * 0.15],
        y=[comm_mid],
        mode='text',
        text='Communication',
        textfont=dict(size=14, color='#D2BA4C', family='Arial Black'),
        showlegend=False,
        hoverinfo='skip'
    ))

# Extend x-axis to show category labels
fig.update_xaxes(range=[-max_hours * 0.25, max_hours * 1.1])

fig.update_traces(cliponaxis=False)

# Save the chart
fig.write_image('component_matrix.png')
fig.write_image('component_matrix.svg', format='svg')