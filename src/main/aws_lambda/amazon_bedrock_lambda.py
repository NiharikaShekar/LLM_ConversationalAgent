import json
import boto3
import base64

def amazon_bedrock_lambda(event, context):

    try:
        # Decoding input
        input_data = base64.b64decode(event.get('body', "")).decode('utf-8')
        req_body = dict(line.split(":", 1) for line in input_data.strip().split("\n"))
        user_input = req_body.get('input', 'A penny saved is a penny earned').strip('"')
        max_words = int(req_body.get('maxWords', 100))

        # Invoking Bedrock model
        client = boto3.client('bedrock-runtime')
        response = client.invoke_model(
            modelId='arn:aws:bedrock:us-east-1:727646466588:inference-profile/us.meta.llama3-1-8b-instruct-v1:0',
            body=json.dumps({"prompt": user_input}),
            contentType='application/json'
        )

        # Processing response
        generated_text = json.loads(response['body'].read().decode('utf-8')).get('generation', "")
        truncated_output = " ".join(generated_text.split()[:max_words])

        return {'statusCode': 200, 'body': json.dumps({'input': user_input, 'output': truncated_output})}

    except Exception as e:
        return {'statusCode': 500, 'body': json.dumps({'error': str(e)})}