import os
import sys
import json
import urllib.request, urllib.parse, urllib.error
import urllib.request, urllib.error, urllib.parse
import argparse
import string
import http.client
import time
from datetime import datetime, date, timedelta

# DC
DC_API = 'localhost'
PORT = '8081'


def query_yes_no(question, default="yes"):
    """Ask a yes/no question via raw_input() and return their answer.
    "question" is a string that is presented to the user.
    "default" is the presumed answer if the user just hits <Enter>.
        It must be "yes" (the default), "no" or None (meaning
        an answer is required of the user).
    The "answer" return value is True for "yes" or False for "no".
    """
    valid = {"yes": True, "y": True, "ye": True,
             "no": False, "n": False}
    if default is None:
        prompt = " [y/n] "
    elif default == "yes":
        prompt = " [Y/n] "
    elif default == "no":
        prompt = " [y/N] "
    else:
        raise ValueError("invalid default answer: '%s'" % default)

    while True:
        sys.stdout.write(question + prompt)
        choice = input().lower()
        if default is not None and choice == '':
            return valid[default]
        elif choice in valid:
            return valid[choice]
        else:
            sys.stdout.write("Please respond with 'yes' or 'no' "
                             "(or 'y' or 'n').\n")


def delete_vm(vmid, guid):
    headers = {'Content-Type': 'application/x-www-form-urlencoded',
               'htrc-remote-user': guid}

    params = urllib.parse.urlencode({'vmid': vmid})

    # POST the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("POST", '/sloan-ws/deletevm', params, headers)
    response = conn.getresponse()

    data = response.read()

    print(data)


def stop_vm(vmid, guid):
    headers = {'Content-Type': 'application/x-www-form-urlencoded',
               'htrc-remote-user': guid}

    params = urllib.parse.urlencode({'vmid': vmid})

    # POST the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("POST", '/sloan-ws/stopvm', params, headers)
    response = conn.getresponse()

    data = response.read()

    print(data)


def start_vm(vmid, guid):
    headers = {'Content-Type': 'application/x-www-form-urlencoded',
               'htrc-remote-user': guid}

    params = urllib.parse.urlencode({'vmid': vmid})

    # POST the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("POST", '/sloan-ws/launchvm', params, headers)
    response = conn.getresponse()

    data = response.read()

    print(data)


def switch_vm(vmid, guid, mode):
    headers = {'Content-Type': 'application/x-www-form-urlencoded',
               'htrc-remote-user': guid}

    params = urllib.parse.urlencode({'vmid': vmid, 'mode': mode})

    # POST the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("POST", '/sloan-ws/switchvm', params, headers)
    response = conn.getresponse()

    data = response.read()

    print(data)


def create_vm(guid, useremail, imagename, loginusername, loginpassword, memory, vcpu, type, concent, full_access,
              title, desc_nature, desc_requirement, desc_links, desc_outside_data, rr_data_files, rr_result_usage):
    headers = {'Content-Type': 'application/x-www-form-urlencoded',
               'htrc-remote-user': guid,
               'htrc-remote-user-email': useremail}

    params = urllib.parse.urlencode(
        {'imagename': imagename, 'loginusername': loginusername, 'loginpassword': loginpassword, 'memory': memory,
         'vcpu': vcpu, 'type': type, 'concent': concent, 'full_access': full_access, 'title': title,
         'desc_nature': desc_nature, 'desc_requirement': desc_requirement, 'desc_links': desc_links,
         'desc_outside_data': desc_outside_data, 'rr_data_files': rr_data_files, 'rr_result_usage': rr_result_usage})

    # POST the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("POST", '/sloan-ws/createvm', params, headers)
    response = conn.getresponse()

    data = response.read()

    print(data)


def show_release():
    headers = {'Content-Type': 'application/json'}

    # GET the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("GET", '/sloan-ws/showreleased')
    response = conn.getresponse()

    data = response.read()
    parsed = json.loads(data)
    print(json.dumps(parsed, indent=4, sort_keys=True))


def show_unrelease():
    headers = {'Content-Type': 'application/json'}

    # GET the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("GET", '/sloan-ws/showunreleased')
    response = conn.getresponse()

    data = response.read()
    parsed = json.loads(data)
    print(json.dumps(parsed, indent=4, sort_keys=True))


def retrieve_file(result_id, out_file):
    # GET the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("GET", '/sloan-ws/retrieveresultfile?randomid=' + result_id)
    response = conn.getresponse()

    if (response.status != 200):
        print(response.read())
    else:
        data = response.read()
        f = open(out_file, 'w')
        f.write(data)
        f.close()
        print('Result written to ' + out_file + ' file...')


def download_file(result_id, out_file):
    # GET the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("GET", '/sloan-ws/download?randomid=' + result_id)
    response = conn.getresponse()

    if (response.status != 200):
        print(response.read())
    else:
        data = response.read()
        f = open(out_file, 'w')
        f.write(data)
        f.close()
        print('Result downloaded to ' + out_file + ' file...')


def update_result(result_id, status):
    params = urllib.parse.urlencode({'resultid': result_id, 'status': status})

    # POST the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("POST", '/sloan-ws/updateresult', params)
    response = conn.getresponse()

    print(response.read())


def stop_running_vms():
    headers = {'Content-Type': 'application/json',
               'Accept': 'application/json'}

    # Get request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("GET", '/sloan-ws/listvms')
    response = conn.getresponse()

    if response.status == 200:
        vms = json.loads(response.read())['vmsInfo']

        for vm in vms:
            if vm["vmState"] == "RUNNING":
                roles = vm["roles"]
                for role in roles:
                    if role["role"] == "OWNER_CONTROLLER" or role["role"] == "OWNER" or role["role"] == "CONTROLLER":
                        print('Stopping VM: {}'.format(vm["vmid"]))
                        stop_vm(vm["vmid"], role["guid"])
                        time.sleep(5)



def update_vmtype(vmid, guid, status):
    owner_guid = None
    owner_email = None
    params = None
    # Get request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("GET", '/sloan-ws/listvms')
    response = conn.getresponse()
    vm_list = []

    if response.status == 200:
        vms = json.loads(response.read())['vmsInfo']

        for vm in vms:
            if vm["vmid"] == vmid:
                roles = vm["roles"]
                for role in roles:
                    if role["guid"] == guid:
                        if role["role"] == "OWNER_CONTROLLER" or role["role"] == "OWNER":
                            owner_guid = guid
                            owner_email = role["email"]
                            params = urllib.parse.urlencode({'vmId': vmid, 'type': 'RESEARCH-FULL', 'full_access': status})
                        else:
                            params = urllib.parse.urlencode({'vmId': vmid, 'type': 'RESEARCH-FULL', 'full_access': status, 'guids': guid})
                    else:
                        if role["role"] == "OWNER_CONTROLLER" or role["role"] == "OWNER":
                            owner_guid = role["guid"]
                            owner_email = role["email"]


    headers = {'Content-Type': 'application/x-www-form-urlencoded',
               'htrc-remote-user': owner_guid, 'htrc-remote-user-email': owner_email}


    # POST the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("POST", '/sloan-ws/updatevm', params, headers)
    response = conn.getresponse()

    data = response.read()

    print(data)


def migrate_vm(guid, vm, dst_host):
    headers = {'Content-Type': 'application/x-www-form-urlencoded',
               'htrc-remote-user': guid}

    params = urllib.parse.urlencode({'vmid': vm, 'host': dst_host})

    # POST the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("POST", '/sloan-ws/migratevm', params, headers)
    response = conn.getresponse()

    data = response.read()

    print(data)


def migrate_all(src_host, dst_host):
    # Get request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("GET", '/sloan-ws/listvms')
    response = conn.getresponse()

    if response.status == 200:
        vms = json.loads(response.read())['vmsInfo']

        for vm in vms:
            if vm["vmState"] == "SHUTDOWN" and vm["host"] == src_host:
                roles = vm["roles"]
                for role in roles:
                    if role["role"] == "OWNER_CONTROLLER" or role["role"] == "OWNER":
                        print('Migrating VM: {}'.format(vm["vmid"]))
                        migrate_vm(role["guid"], vm["vmid"], dst_host)
                        time.sleep(5)


def show_capsules(guid):
    headers = {'Content-Type': 'application/x-www-form-urlencoded',
               'htrc-remote-user': guid}

    # POST the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("POST", '/sloan-ws/show', "", headers)
    response = conn.getresponse()

    data = response.read()
    parsed = json.loads(data)
    print(json.dumps(parsed, indent=4, sort_keys=True))


def show_pending_fullaccess():
    # Get request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("GET", '/sloan-ws/listvms')
    response = conn.getresponse()
    vm_list = []

    if response.status == 200:
        vms = json.loads(response.read())['vmsInfo']

        for vm in vms:
            roles = vm["roles"]
            for role in roles:
                if role["full_access"] is not None and role["full_access"] is False:
                    if role["role"] == "OWNER_CONTROLLER" or role["role"] == "OWNER":
                        vm_list.append(vm["vmid"])
                        print('VM ID : {} User : {} Role: {} Email: {} has pending request for full data access.'.format(vm["vmid"], role["guid"], role["role"], role["email"]))

            for role in roles:
                if role["full_access"] is not None and role["full_access"] is False and not vm_list.__contains__(vm["vmid"]):
                    print('VM ID : {} User : {} Role: {} Email: {} has pending request for full data access.'.format(vm["vmid"], role["guid"], role["role"], role["email"]))



def delete_expired_capsules():
    # Get request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("GET", '/sloan-ws/listvms')
    response = conn.getresponse()

    if response.status == 200:
        vms = json.loads(response.read())['vmsInfo']

        for vm in vms:
            if vm["type"] == "DEMO":
                created_date = datetime.strptime(vm["created_at"],"%Y-%m-%d %H:%M:%S").date()
                expired_date = created_date + timedelta(days=32)
                if expired_date < date.today():
                    roles = vm["roles"]
                    for role in roles:
                        if role["role"] == "OWNER_CONTROLLER" or role["role"] == "OWNER":
                            print('Deleting Expired Capsule : {} owned by : {} Email: {}'.format(vm["vmid"] , role["guid"], role["email"]))
                            delete_vm(vm["vmid"] , role["guid"])
                            time.sleep(5)

def delete_result(result_id):
    params = urllib.parse.urlencode({'resultid': result_id})

    # POST the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("DELETE", '/sloan-ws/deleteresult', params)
    response = conn.getresponse()

    data = response.read()

    print(data)

def delete_expired_results():
    # GET request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("GET", '/sloan-ws/showreleased')
    response = conn.getresponse()

    if response.status == 200:
        results = json.loads(response.read())['reviewInfo']

        for result in results:
            if result["state"] != "DELETED":
                if result["notifiedtime"]:
                    notified_date = datetime.strptime(result["notifiedtime"].split('.')[0],"%Y-%m-%d %H:%M:%S").date()
                    # Rejected results will be deleted after 2 weeks from the notified date
                    if result["status"] == "Rejected":
                        expired_date = notified_date + timedelta(days=14)
                        if expired_date < date.today():
                            roles = result["roles"]
                            for role in roles:
                                if role["role"] == "OWNER_CONTROLLER" or role["role"] == "OWNER":
                                    print('Deleting Expired resultID: {} result status: {} notified on: {} capsuleID: {} owned by: {} Email: {}'.format(result["resultid"] , result["status"], result["notifiedtime"], result["vmid"], role["guid"], role["email"]))
                                    delete_result(result["resultid"])
                                    time.sleep(15)

                    # Released results will be deleted after 18 months from the notified date
                    if result["status"] == "Released":
                        expired_date = notified_date + timedelta(days=548)
                        if expired_date < date.today():
                            roles = result["roles"]
                            for role in roles:
                                if role["role"] == "OWNER_CONTROLLER" or role["role"] == "OWNER":
                                    print('Deleting Expired resultID: {} result status: {} notified on: {} capsuleID: {} owned by: {} Email: {}'.format(result["resultid"] , result["status"], result["notifiedtime"], result["vmid"], role["guid"], role["email"]))
                                    delete_result(result["resultid"])
                                    time.sleep(15)
                else:
                    print('Notified time is null resultID: {} result status: {} notified on: {} capsuleID: {}'.format(result["resultid"] , result["status"], result["notifiedtime"], result["vmid"]))


def add_sharee(vm,guid, useremail, sharee_guid, sharee_email, sharee_desc):
    headers = {'Content-Type': 'application/x-www-form-urlencoded',
               'htrc-remote-user': guid,
               'htrc-remote-user-email': useremail}

    sharees = "[{guid:'" + sharee_guid + "', email : '" + sharee_email + "'}]"

    params = urllib.parse.urlencode(
        {'vmId': vm,'sharees': sharees, 'desc_shared': sharee_desc})

    # POST the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("POST", '/sloan-ws/addsharees', params, headers)
    response = conn.getresponse()

    data = response.read()
    parsed = json.loads(data)
    print(json.dumps(parsed, indent=4, sort_keys=True))


def delete_sharee(vm,guid, sharee_guid):
    headers = {'Content-Type': 'application/x-www-form-urlencoded',
               'htrc-remote-user': guid}

    params = urllib.parse.urlencode(
        {'vmId': vm,'sharees': sharee_guid})

    # POST the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("POST", '/sloan-ws/deletesharees', params, headers)
    response = conn.getresponse()

    data = response.read()
    print(data)

def update_tou(vm,guid):
    headers = {'Content-Type': 'application/x-www-form-urlencoded',
               'htrc-remote-user': guid}

    params = urllib.parse.urlencode(
        {'vmId': vm,'tou': True})

    # POST the request
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("POST", '/sloan-ws/updateusertou', params, headers)
    response = conn.getresponse()

    data = response.read()
    print(data)

def add_htrc_help_user(sharee_guid, sharee_email):
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("GET", '/sloan-ws/listvms')
    response = conn.getresponse()

    if response.status == 200:
        vms = json.loads(response.read())['vmsInfo']

        for vm in vms:
            if vm["vmState"] == "SHUTDOWN":
                roles = vm["roles"]
                for role in roles:
                    if role["role"] == "OWNER_CONTROLLER" or role["role"] == "OWNER":
                        print("Add HTRC help user to capsule ID: " + vm["vmid"])
                        add_sharee(vm["vmid"], role["guid"], role["email"], sharee_guid, sharee_email, "HTRC help user.")
                        update_tou(vm["vmid"],sharee_guid)
                        if vm["type"] == "RESEARCH-FULL":
                            update_vmtype(vm["vmid"],sharee_guid,True)

def remove_htrc_help_user(sharee_guid):
    conn = http.client.HTTPConnection(DC_API, PORT)
    conn.request("GET", '/sloan-ws/listvms')
    response = conn.getresponse()

    if response.status == 200:
        vms = json.loads(response.read())['vmsInfo']

        for vm in vms:
            if vm["vmState"] == "SHUTDOWN":
                roles = vm["roles"]
                for role in roles:
                    if role["role"] == "OWNER_CONTROLLER" or role["role"] == "OWNER":
                        print("Remove HTRC help user from capsule ID: " + vm["vmid"])
                        delete_sharee(vm["vmid"], role["guid"], sharee_guid)





if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    subparsers = parser.add_subparsers(dest='sub_commands')
    delete = subparsers.add_parser('delete', description='Delete DC VM')
    delete.add_argument('vm')
    delete.add_argument('guid')

    stop = subparsers.add_parser('stop', description='Stop DC VM')
    stop.add_argument('vm')
    stop.add_argument('guid')

    start = subparsers.add_parser('start', description='Start DC VM')
    start.add_argument('vm')
    start.add_argument('guid')

    switch = subparsers.add_parser('switch', description='Switch DC VM')
    switch.add_argument('vm')
    switch.add_argument('guid')
    switch.add_argument('mode')

    create = subparsers.add_parser('create', description='Create DC VM')
    create.add_argument('guid')
    create.add_argument('vmuseremail')
    create.add_argument('imagename')
    create.add_argument('vncusername')
    create.add_argument('vncpassword')
    create.add_argument('memory')
    create.add_argument('vcpu')
    create.add_argument('type')
    create.add_argument('concent')
    create.add_argument('full_access')
    create.add_argument('title')
    create.add_argument('desc_nature')
    create.add_argument('desc_requirement')
    create.add_argument('desc_links')
    create.add_argument('desc_outside_data')
    create.add_argument('rr_data_files')
    create.add_argument('rr_result_usage')

    showrelease = subparsers.add_parser('showrelease', description='Show released results')

    showunrelease = subparsers.add_parser('showunrelease', description='Show un-released results')

    retrievefile = subparsers.add_parser('retrievefile', description='Get result file')
    retrievefile.add_argument('rid')
    retrievefile.add_argument('filename')

    downloadfile = subparsers.add_parser('downloadfile', description='Download result file')
    downloadfile.add_argument('rid')
    downloadfile.add_argument('filename')

    releaseresult = subparsers.add_parser('releaseresult', description='Release the result')
    releaseresult.add_argument('rid')

    rejectresult = subparsers.add_parser('rejectresult', description='Reject the result')
    rejectresult.add_argument('rid')

    subparsers.add_parser("stoprunning", description="Stop all running capsules")

    approvefullaccess = subparsers.add_parser('approvefullaccess', description='Approve Full Access for Data')
    approvefullaccess.add_argument('vm')
    approvefullaccess.add_argument('guid')

    rejectfullaccess = subparsers.add_parser('rejectfullaccess', description='Reject Full Access for Data')
    rejectfullaccess.add_argument('vm')
    rejectfullaccess.add_argument('guid')

    showcapsules = subparsers.add_parser('showcapsules', description='Show all capsules for the given user')
    showcapsules.add_argument('guid')

    showpendingfullaccess = subparsers.add_parser('showpendingfullaccess', description='Show VM IDs which have pending requests for full data access.')

    migrateall = subparsers.add_parser('migrateall', description='Migrate all VMs from one host to another.')
    migrateall.add_argument('srchost')
    migrateall.add_argument('desthost')


    migrate = subparsers.add_parser('migrate', description='Migrate VM from one host to another.')
    migrate.add_argument('guid')
    migrate.add_argument('vm')
    migrate.add_argument('desthost')

    deleteexpiredcapsules = subparsers.add_parser('deleteexpiredcapsules', description='Delete expired capsules.')

    deleteresult = subparsers.add_parser('deleteresult', description='Delete a rejected or released result.')
    deleteresult.add_argument('rid')

    deleteexpiredresults = subparsers.add_parser('deleteexpiredresults', description='Delete expired results.')

    addsharee = subparsers.add_parser('addsharee', description='Add a sharee.')
    addsharee.add_argument('vm')
    addsharee.add_argument('owner_guid')
    addsharee.add_argument('owner_email')
    addsharee.add_argument('sharee_guid')
    addsharee.add_argument('sharee_email')
    addsharee.add_argument('sharee_description')

    deletesharee = subparsers.add_parser('deletesharee', description='Delete a sharee.')
    deletesharee.add_argument('vm')
    deletesharee.add_argument('owner_guid')
    deletesharee.add_argument('sharee_guid')
    deletesharee.add_argument('sharee_email')

    updatetou = subparsers.add_parser('updatetou', description='Update capsule TOU.')
    updatetou.add_argument('vm')
    updatetou.add_argument('owner_guid')

    addhelpuser = subparsers.add_parser('addhelpuser', description='Add HTRC help user to all capsules.')
    addhelpuser.add_argument('sharee_guid')
    addhelpuser.add_argument('sharee_email')

    removehelpuser = subparsers.add_parser('removehelpuser', description='Remove HTRC help user from all capsules.')
    removehelpuser.add_argument('sharee_guid')
    removehelpuser.add_argument('sharee_email')

    parsed = parser.parse_args()

    if parsed.sub_commands == 'delete':
        confirmation = query_yes_no(
            'Are you sure you want to delete the VM ' + parsed.vm + '? This operation is not recoverable.')
        if confirmation:
            print('Deleting  VM ' + parsed.vm + '....')
            delete_vm(parsed.vm, parsed.guid)

    if parsed.sub_commands == 'stop':
        confirmation = query_yes_no('Are you sure you want to stop the VM ' + parsed.vm + '?')
        if confirmation:
            print('Stopping  VM ' + parsed.vm + '....')
            stop_vm(parsed.vm, parsed.guid)

    if parsed.sub_commands == 'start':
        confirmation = query_yes_no('Are you sure you want to start the VM ' + parsed.vm + '?')
        if confirmation:
            print('Starting  VM ' + parsed.vm + '....')
            start_vm(parsed.vm, parsed.guid)

    if parsed.sub_commands == 'switch':
        confirmation = query_yes_no(
            'Are you sure you want to switch the VM ' + parsed.vm + ' to ' + parsed.mode + ' mode?')
        if confirmation:
            print('Switching  VM ' + parsed.vm + ' to ' + parsed.mode + '....')
            switch_vm(parsed.vm, parsed.guid, parsed.mode)

    if parsed.sub_commands == 'create':
        confirmation = query_yes_no(
            'Are you sure you want to create a VM with image: ' + parsed.imagename + ', memory: ' + parsed.memory + ', vcpu: ' + parsed.vcpu + ' ?')
        if confirmation:
            print('Creating  VM with image:' + parsed.imagename + ', VNC User name:' + parsed.vncusername + ', VNC Password:' + parsed.vncpassword + ', memory: ' + parsed.memory + ', vcpu: ' + parsed.vcpu + '...')
            create_vm(parsed.guid, parsed.vmuseremail, parsed.imagename, parsed.vncusername, parsed.vncpassword,
                      parsed.memory, parsed.vcpu, parsed.type, parsed.concent, parsed.full_access, parsed.title, parsed.desc_nature,
                      parsed.desc_requirement, parsed.desc_links, parsed.desc_outside_data, parsed.rr_data_files, parsed.rr_result_usage)

    if parsed.sub_commands == 'showrelease':
        print('Released Results:')
        show_release()

    if parsed.sub_commands == 'showunrelease':
        print('Un-Released Results:')
        show_unrelease()

    if parsed.sub_commands == 'retrievefile':
        confirmation = query_yes_no('Are you sure you want to retrieve file with id ' + parsed.rid)
        if confirmation:
            print('Retrieving file for result ' + parsed.rid + '....')
            retrieve_file(parsed.rid, parsed.filename)

    if parsed.sub_commands == 'downloadfile':

        confirmation = query_yes_no('Are you sure you want to download file with id ' + parsed.rid)
        if confirmation:
            print('Download file for result ' + parsed.rid + '....')
            download_file(parsed.rid, parsed.filename)

    if parsed.sub_commands == 'releaseresult':

        confirmation = query_yes_no('Are you sure you want to release the result with id ' + parsed.rid)
        if confirmation:
            print('Release result ' + parsed.rid + '....')
            update_result(parsed.rid, 'Released')

    if parsed.sub_commands == 'rejectresult':

        confirmation = query_yes_no('Are you sure you want to reject the result with id ' + parsed.rid)
        if confirmation:
            print('Reject result ' + parsed.rid + '....')
            update_result(parsed.rid, 'Rejected')

    if parsed.sub_commands == 'stoprunning':
        print('Stopping all the running VMs')
        stop_running_vms()

    if parsed.sub_commands == 'approvefullaccess':
        print('Giving full access for  VM ' + parsed.vm + '....')
        update_vmtype(parsed.vm, parsed.guid, 'true')

    if parsed.sub_commands == 'rejectfullaccess':
        confirmation = query_yes_no('Are you sure you want to reject full access to VM ' + parsed.vm + '?')
        if confirmation:
            print('Rejecting full access for  VM ' + parsed.vm + '....')
            update_vmtype(parsed.vm, parsed.guid, 'false')

    if parsed.sub_commands == 'showcapsules':
        print('Showing capsules information for GUID ' + parsed.guid + '....')
        show_capsules(parsed.guid)

    if parsed.sub_commands == 'showpendingfullaccess':
        print('Showing capsules list which has pending requests for full data access.')
        show_pending_fullaccess()

    if parsed.sub_commands == 'migrateall':
        print('Migrating all the capsules in {} to {}'.format(parsed.srchost, parsed.desthost))
        migrate_all(parsed.srchost, parsed.desthost)

    if parsed.sub_commands == 'migrate':
        print('Migrating VM {} to {}'.format(parsed.vm, parsed.desthost))
        migrate_vm(parsed.guid, parsed.vm, parsed.desthost)

    if parsed.sub_commands == 'deleteexpiredcapsules':
        delete_expired_capsules()

    if parsed.sub_commands == 'deleteresult':
        confirmation = query_yes_no('Are you sure you want to delete result ID ' + parsed.rid + '?')
        if confirmation:
            print('Deleting result ID ' + parsed.rid + '....')
            delete_result(parsed.rid)

    if parsed.sub_commands == 'deleteexpiredresults':
        delete_expired_results()

    if parsed.sub_commands == 'addsharee':
        print('Adding Sharee ' + parsed.sharee_email + '....')
        add_sharee(parsed.vm,parsed.owner_guid,parsed.owner_email,parsed.sharee_guid,parsed.sharee_email,parsed.sharee_description)

    if parsed.sub_commands == 'deletesharee':
        confirmation = query_yes_no('Are you sure you want to delete sharee ' + parsed.sharee_email + '?')
        if confirmation:
            print('Deleting Sharee ' + parsed.sharee_email + '....')
            delete_sharee(parsed.vm,parsed.owner_guid,parsed.sharee_guid)

    if parsed.sub_commands == 'updatetou':
        print('Update TOU for ' + parsed.owner_guid + '....')
        update_tou(parsed.vm,parsed.owner_guid)

    if parsed.sub_commands == 'addhelpuser':
        print('Add HTRC help user ' + parsed.sharee_email + ' to all capsules.')
        add_htrc_help_user(parsed.sharee_guid,parsed.sharee_email)

    if parsed.sub_commands == 'removehelpuser':
        print('Remove HTRC help user ' + parsed.sharee_email + ' from all capsules.')
        remove_htrc_help_user(parsed.sharee_guid)
